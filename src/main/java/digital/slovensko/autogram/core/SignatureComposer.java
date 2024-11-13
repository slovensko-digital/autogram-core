package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.DataToSignStructure;
import eu.europa.esig.dss.model.DSSDocument;

public interface SignatureComposer {
    void buildSignedDocument(DSSDocument document, SigningParameters parameters, DataToSignStructure dataToSignStructure, String signedData, Responder responder);

    DataToSignStructure getDataToSign(DSSDocument document, SigningParameters parameters, String signingCertificate);
}
