package digital.slovensko.autogram.core.validation;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.util.XMLUtils;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.function.TLPredicateFactory;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.sync.ExpirationAndSignatureCheckStrategy;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static digital.slovensko.autogram.core.util.DSSUtils.*;

public class SignatureValidator {
    private static final String LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    private static final String OJ_URL = "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2019.276.01.0001.01.ENG";
    private CertificateVerifier verifier;
    private TLValidationJob validationJob;
    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureValidator.class);

    // Singleton
    private static SignatureValidator instance;

    private SignatureValidator() {
    }

    public synchronized static SignatureValidator getInstance() {
        if (instance == null)
            instance = new SignatureValidator();

        return instance;
    }

    private synchronized Reports validate(SignedDocumentValidator docValidator) {
        docValidator.setCertificateVerifier(verifier);

        // TODO: do not print stack trace inside DSS
        return docValidator.validateDocument();
    }

    public synchronized CertificateVerifier getVerifier() {
        return verifier;
    }

    public synchronized ReportsAndValidator validate(DSSDocument document) {
        var documentValidator = createDocumentValidator(document);
        if (documentValidator == null)
            return null;

        try {
            return new ReportsAndValidator(validate(documentValidator), documentValidator);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static void scheduleRefresh(ScheduledExecutorService executorService, long initialDelayMinutes, long periodMinutes) {
        executorService.scheduleAtFixedRate(() -> getInstance().refresh(), initialDelayMinutes, periodMinutes, java.util.concurrent.TimeUnit.MINUTES);
    }

    public synchronized void refresh() {
        validationJob.offlineRefresh();
    }

    public synchronized void initialize(ExecutorService executorService, List<String> tlCountries) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        LOGGER.debug("Initializing signature validator at {}", formatter.format(new Date()));

        validationJob = new TLValidationJob();

        var lotlSource = new LOTLSource();
        lotlSource.setCertificateSource(getJournalCertificateSource());
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(OJ_URL));
        lotlSource.setUrl(LOTL_URL);
        lotlSource.setPivotSupport(true);
        lotlSource.setTlPredicate(TLPredicateFactory.createEUTLCountryCodePredicate(tlCountries.toArray(new String[0])));

        var offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(21600000);  // 6 hours
        offlineFileLoader.setDataLoader(new CommonsDataLoader());
        validationJob.setOfflineDataLoader(offlineFileLoader);

        var onlineFileLoader = new FileCacheDataLoader();
        onlineFileLoader.setCacheExpirationTime(0);
        onlineFileLoader.setDataLoader(new CommonsDataLoader());
        validationJob.setOnlineDataLoader(onlineFileLoader);

        var trustedListCertificateSource = new TrustedListsCertificateSource();
        validationJob.setTrustedListCertificateSource(trustedListCertificateSource);
        validationJob.setListOfTrustedListSources(lotlSource);
        validationJob.setSynchronizationStrategy(new ExpirationAndSignatureCheckStrategy());
        validationJob.setExecutorService(executorService);
        validationJob.setDebug(false);

        LOGGER.debug("Starting signature validator offline refresh");
        validationJob.offlineRefresh();

        verifier = new CommonCertificateVerifier();
        verifier.setTrustedCertSources(trustedListCertificateSource);
        verifier.setCrlSource(new OnlineCRLSource());
        verifier.setOcspSource(new OnlineOCSPSource());

        LOGGER.debug("Signature validator initialized at {}", formatter.format(new Date()));
    }

    private CertificateSource getJournalCertificateSource() throws AssertionError {
        try {
            var keystore = getClass().getResourceAsStream("lotlKeyStore.p12");
            return new KeyStoreCertificateSource(keystore, "PKCS12", "dss-password".toCharArray());

        } catch (DSSException | NullPointerException e) {
            throw new AssertionError("Cannot load LOTL keystore", e);
        }
    }

    public synchronized ValidationReports getSignatureValidationReport(SigningJob job) {
        var documentValidator = createDocumentValidator(job.getDocument());
        if (documentValidator == null)
            return new ValidationReports(null, job);

        return new ValidationReports(validate(documentValidator), job);
    }

    public static String getSignatureValidationReportHTML(Reports signatureValidationReport) {
        try {
            var document = XMLUtils.getSecureDocumentBuilder().parse(new InputSource(new StringReader(signatureValidationReport.getXmlSimpleReport())));
            var xmlSource = new DOMSource(document);

            var xsltFile = SignatureValidator.class.getResourceAsStream("simple-report-bootstrap4.xslt");
            var xsltSource = new StreamSource(xsltFile);

            var outputTarget = new StreamResult(new StringWriter());
            var transformer = XMLUtils.getSecureTransformerFactory().newTransformer(xsltSource);
            transformer.transform(xmlSource, outputTarget);

            var r = outputTarget.getWriter().toString().trim();

            var templateFile = SignatureValidator.class.getResourceAsStream("simple-report-template.html");
            var templateString = new String(templateFile.readAllBytes(), StandardCharsets.UTF_8);
            return templateString.replace("{{content}}", r);

        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            return "Error transforming validation report";
        }
    }

    public static ValidationReports getSignatureCheckReport(SigningJob job) {
        var validator = createDocumentValidator(job.getDocument());
        if (validator == null)
            return new ValidationReports(null, job);

        validator.setCertificateVerifier(new CommonCertificateVerifier());
        return new ValidationReports(validator.validateDocument(), job);
    }

    public static SimpleReport getSignedDocumentSimpleReport(DSSDocument document) {
        var validator = createDocumentValidator(document);
        if (validator == null)
            return null;

        validator.setCertificateVerifier(new CommonCertificateVerifier());
        var report = validator.validateDocument().getSimpleReport();
        if (report.getSignatureIdList().size() == 0)
            return null;

        return report;
    }

    public static SignatureLevel getSignedDocumentSignatureLevel(SimpleReport report) {
        if (report == null)
            return null;

        return report.getSignatureFormat(report.getSignatureIdList().get(0));
    }

    public synchronized boolean areTLsLoaded() {
        // TODO: consider validation turned off as well
        return validationJob.getSummary().getNumberOfProcessedTLs() > 0;
    }

    public int loadedTLs() {
        if (validationJob == null)
            return 0;

        return validationJob.getSummary().getNumberOfProcessedTLs();
    }

}
