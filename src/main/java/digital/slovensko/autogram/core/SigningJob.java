package digital.slovensko.autogram.core;

import java.io.File;
import java.util.Base64;
import java.util.Date;

import digital.slovensko.autogram.core.dto.DataToSignStructure;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.dto.SigningKey;
import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.xdc.XDCBuilder;
import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import digital.slovensko.autogram.core.errors.*;
import digital.slovensko.autogram.core.util.DSSUtils;
import digital.slovensko.autogram.core.validation.SignatureValidator;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.*;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

import static digital.slovensko.autogram.core.AutogramMimeType.*;

public class SigningJob {
    private final Responder responder;
    private final DSSDocument document;
    private final SigningParameters parameters;

    private SigningJob(DSSDocument document, SigningParameters parameters, Responder responder) {
        this.document = document;
        this.parameters = parameters;
        this.responder = responder;
    }

    public DSSDocument getDocument() {
        return this.document;
    }

    public SigningParameters getParameters() {
        return parameters;
    }

    public int getVisualizationWidth() {
        return parameters.getVisualizationWidth();
    }

    public void onDocumentSignFailed(AutogramException e) {
        responder.onError(e);
    }

    public void signWithKeyAndRespond(SigningKey signingKey, TSPSource tspSource) {
        var dataToSign = buildDataToSign(signingKey.getCertificate());
        var signedData = signingKey.sign(new ToBeSigned(Base64.getDecoder().decode(dataToSign.dataToSign().getBytes())), getParameters().getDigestAlgorithm());
        var signedDocument = buildSignedDocument(dataToSign, new String(Base64.getEncoder().encode(signedData.getValue())), tspSource);

        responder.onSuccess(signedDocument);
    }

    public void signWithSignedDataAndRespond(DataToSignStructure dataToSignStructure, String signedData, TSPSource tspSource) throws AutogramException {
        var signedDocument = buildSignedDocument(dataToSignStructure, signedData, tspSource);

        responder.onSuccess(signedDocument);
    }

    private SignedDocument buildSignedDocument(DataToSignStructure dataToSignStructure, String signedData, TSPSource tspSource) throws AutogramException {
        CertificateToken token;
        token = DSSUtils.parseCertificate(dataToSignStructure.signingCertificate());

        var signatureValue = new SignatureValue(token.getSignatureAlgorithm(), Base64.getDecoder().decode(signedData));
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var signatureParameters = parameters.getSignatureParameters();
        var service = DSSUtils.getServiceForSignatureLevel(parameters.getSignatureType(), parameters.getContainer(), commonCertificateVerifier);

        signatureParameters.setSigningCertificate(token);
        signatureParameters.setCertificateChain(token);
        var bLevelParameters = new BLevelParameters();
        bLevelParameters.setSigningDate(new Date(dataToSignStructure.signingTime()));
        signatureParameters.setBLevelParams(bLevelParameters);

        if (!BaselineLevel.build(signatureParameters.getSignatureLevel()).equals(BaselineLevel.B))
            service.setTspSource(tspSource);

        try {
            if (signatureParameters.getSignatureLevel().equals(SignatureLevel.PAdES_BASELINE_T)) {
                ((PAdESSignatureParameters) signatureParameters).setContentSize(9472 * 2);
            }

            var dataToSign = service.getDataToSign(document, signatureParameters);
            if (!new String(Base64.getEncoder().encode(dataToSign.getBytes())).equals(dataToSignStructure.dataToSign()))
                throw new DataToSignMismatchException();

            DSSDocument doc;
            try {
                doc = service.signDocument(document, signatureParameters, signatureValue);
                doc.setName(DSSUtils.generatePrettyName(doc.getName(), document.getName()));
            } catch (DSSException e) {
                if (e.getMessage().contains("Cryptographic signature verification has failed"))
                    throw new CryptographicSignatureVerificationException();

                throw e;
            }

            return new SignedDocument(doc, token);

        } catch (IllegalArgumentException e) {
            throw AutogramException.createFromIllegalArgumentException(e);

        } catch (DSSException e) {
            for (Throwable cause = e; cause != null && cause.getCause() != cause; cause = cause.getCause()) {
                if (cause.getMessage() == null)
                    continue;

                if (cause instanceof DSSExternalResourceException) {
                    throw new TsaServerMisconfiguredException("Nastavený TSA server odmietol pridať časovú pečiatku. Skontrolujte nastavenia TSA servera.", cause);
                } else if (cause instanceof NullPointerException && cause.getMessage().contains("Host name")) {
                    throw new TsaServerMisconfiguredException("Nie je nastavená žiadna adresa TSA servera. Skontrolujte nastavenia TSA servera.", cause);
                }
            }

            throw new UnrecognizedException(e);
        }
    }

    public DataToSignStructure buildDataToSign(CertificateToken token) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var signatureParameters = parameters.getSignatureParameters();
        var service = DSSUtils.getServiceForSignatureLevel(parameters.getSignatureType(), parameters.getContainer(), commonCertificateVerifier);
        var signingTime = new Date();

        signatureParameters.setSigningCertificate(token);

        var bLevelParameters = new BLevelParameters();
        bLevelParameters.setSigningDate(signingTime);
        signatureParameters.setBLevelParams(bLevelParameters);

        if (signatureParameters.getSignatureLevel().equals(SignatureLevel.PAdES_BASELINE_T)) {
            ((PAdESSignatureParameters)signatureParameters).setContentSize(9472*2);
        }

        var dataToSign = Base64.getEncoder().encode(service.getDataToSign(document, signatureParameters).getBytes());

        return new DataToSignStructure(new String(dataToSign), signingTime.getTime(), DSSUtils.encodeCertificate(token));
    }

    public static FileDocument createDSSFileDocumentFromFile(File file) {
        var fileDocument = new FileDocument(file);

        if (fileDocument.getName().endsWith(".xdcf"))
            fileDocument.setMimeType(XML_DATACONTAINER_WITH_CHARSET);

        else if (isXDC(fileDocument.getMimeType()) || isXML(fileDocument.getMimeType()) && XDCValidator.isXDCContent(fileDocument))
            fileDocument.setMimeType(AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET);

        else if (isTxt(fileDocument.getMimeType()))
            fileDocument.setMimeType(AutogramMimeType.TEXT_WITH_CHARSET);

        return fileDocument;
    }

    private static SigningJob build(DSSDocument document, SigningParameters params, Responder responder) {
        if (params.shouldCreateXdc() && !isXDC(document.getMimeType()) && !isAsice(document.getMimeType()))
            document = XDCBuilder.transform(params, document.getName(), EFormUtils.getXmlFromDocument(document));

        if (isTxt(document.getMimeType()))
            document.setMimeType(AutogramMimeType.TEXT_WITH_CHARSET);

        if (isXDC(document.getMimeType())) {
            document.setMimeType(AutogramMimeType.XML_DATACONTAINER_WITH_CHARSET);
            document.setName(DSSUtils.getXdcfFilename(document.getName()));
        }

        return new SigningJob(document, params, responder);
    }

    public static SigningJob buildFromRequest(DSSDocument document, SigningParameters params, Responder responder) {
        return build(document, params, responder);
    }

    public static SigningJob buildFromFile(File file, Responder responder, boolean checkPDFACompliance, SignatureForm pdfSignatureForm, boolean isEn319132, BaselineLevel baselineLevel, boolean plainXmlEnabled) {
        var document = createDSSFileDocumentFromFile(file);
        var parameters = getParametersForFile(document, checkPDFACompliance, pdfSignatureForm, isEn319132, baselineLevel, plainXmlEnabled);
        return build(document, parameters, responder);
    }

    private static SigningParameters getParametersForFile(FileDocument document, boolean checkPDFACompliance, SignatureForm pdfSignatureForm, boolean isEn319132, BaselineLevel baselineLevel, boolean plainXmlEnabled) {
        var level = SignatureValidator.getSignedDocumentSignatureLevel(SignatureValidator.getSignedDocumentSimpleReport(document));
        if (level != null) switch (level.getSignatureForm()) {
            case PAdES:
                return SigningParameters.buildForPDF(document, checkPDFACompliance, isEn319132, baselineLevel);
            case XAdES:
                return SigningParameters.buildForASiCWithXAdES(document, checkPDFACompliance, isEn319132, baselineLevel, plainXmlEnabled);
            case CAdES:
                return SigningParameters.buildForASiCWithCAdES(document, checkPDFACompliance, isEn319132, baselineLevel, plainXmlEnabled);
            default:
                ;
        }

        if (isPDF(document.getMimeType())) switch (pdfSignatureForm) {
            case PAdES:
                return SigningParameters.buildForPDF(document, checkPDFACompliance, isEn319132, baselineLevel);
            case XAdES:
                return SigningParameters.buildForASiCWithXAdES(document, checkPDFACompliance, isEn319132, baselineLevel, plainXmlEnabled);
            case CAdES:
                return SigningParameters.buildForASiCWithCAdES(document, checkPDFACompliance, isEn319132, baselineLevel, plainXmlEnabled);
            default:
                ;
        }

        return SigningParameters.buildForASiCWithXAdES(document, checkPDFACompliance, isEn319132, baselineLevel, plainXmlEnabled);
    }

    public boolean shouldCheckPDFCompliance() {
        return parameters.getCheckPDFACompliance() && isPDF(document.getMimeType());
    }
}
