package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SigningParameters;
import eu.europa.esig.dss.model.DSSDocument;

public interface VisualizationBuilder {
    void buildVisualizationAndRespond(DSSDocument document, SigningParameters signingParameters, Responder responder);

    boolean isPlainXmlEnabled();
}
