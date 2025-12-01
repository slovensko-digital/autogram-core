package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.DSSDocument;

public interface SignatureExtender {
    void extendDocument(DSSDocument document, BaselineLevel targetLevel, Responder responder);
}
