package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.BaselineLevel;
import digital.slovensko.autogram.core.Responder;
import eu.europa.esig.dss.model.DSSDocument;

public interface SignatureExtender {
    void extendDocument(DSSDocument document, BaselineLevel targetLevel, Responder responder);
}
