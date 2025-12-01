package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.DSSDocument;

public interface VisualizationBuilder {
    void buildVisualizationAndRespond(DSSDocument document, SigningParameters signingParameters, Responder responder);

    boolean isPlainXmlEnabled();
}
