package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.model.DSSDocument;

public class PDFVisualization extends Visualization {
    private final DSSDocument document;

    public PDFVisualization(DSSDocument document, SigningJob job) {
        super(job);
        this.document = document;
    }

    @Override
    public DSSDocument getDocument() {
        return document;
    }
}