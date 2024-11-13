package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.errors.UnsupportedSignatureLevelException;
import digital.slovensko.autogram.core.util.DSSUtils;
import digital.slovensko.autogram.core.validation.SignatureValidator;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;

public class ExtendingJob {
    private final DSSDocument document;
    private final Responder responder;
    private final SignatureValidator signatureValidator;
    private final Settings settings;
    private final BaselineLevel targetLevel;


    public static ExtendingJob build(DSSDocument document, BaselineLevel targetLevel, Responder responder, SignatureValidator signatureValidator, Settings settings) {
        return new ExtendingJob(document, targetLevel, responder, signatureValidator, settings);
    }

    private ExtendingJob(DSSDocument document, BaselineLevel targetLevel, Responder responder, SignatureValidator signatureValidator, Settings settings) {
        this.document = document;
        this.responder = responder;
        this.signatureValidator = signatureValidator;
        this.settings = settings;
        this.targetLevel = targetLevel;
    }

    public void extendDocumentAndRespond(BaselineLevel targetLevelOverride) {
        try {
            var extendedDocument = extendDocument(targetLevelOverride);
            responder.onSuccess(extendedDocument);
        } catch (AutogramException e) {
            responder.onError(e);
        } catch (Exception e) {
            responder.onError(new UnrecognizedException(e));
        }
    }

    private SignedDocument extendDocument(BaselineLevel targetLevelOverride) {
        if (targetLevelOverride == null)
            targetLevelOverride = targetLevel;

        var reportsAndValidator = signatureValidator.validate(document);
        if (reportsAndValidator == null)
            throw new DocumentNotSignedYetException();

        var simpleReport = reportsAndValidator.reports().getSimpleReport();
        var level = simpleReport.getSignatureFormat(simpleReport.getFirstSignatureId());
        var isContainer = simpleReport.getContainerType() != null;

        var service = DSSUtils.getServiceForSignatureLevel(level.getSignatureForm(), simpleReport.getContainerType(), signatureValidator.getVerifier());
        service.setTspSource(settings.getTspSource());

        var params = getTimestampParametersForDocument(level.getSignatureForm(), isContainer);
        var targetSignatureLevel = targetLevelOverride.getSignatureLevel(level);
        params.setSignatureLevel(targetSignatureLevel);

        return new SignedDocument(service.extendDocument(document, params), params.getSigningCertificate());
    }

    private static AbstractSignatureParameters getTimestampParametersForDocument(SignatureForm form, boolean isContainer) {
        return switch (form) {
            case XAdES -> isContainer ? new ASiCWithXAdESSignatureParameters() : new XAdESSignatureParameters();
            case CAdES -> isContainer ? new ASiCWithCAdESSignatureParameters() : new CAdESSignatureParameters();
            case PAdES -> new PAdESSignatureParameters();
            default -> throw new UnsupportedSignatureLevelException(form.name());
        };
    }
}
