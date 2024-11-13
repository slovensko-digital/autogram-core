package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.model.DSSDocument;

public class UnsupportedVisualization extends Visualization {
    public UnsupportedVisualization(SigningJob job) {
        super(job);
    }

    public void initialize(Visualizer visualizer) {
        // no pref width, keep default
        visualizer.showUnsupportedVisualization();
    }

    public DSSDocument getDocument() {
        return null;
    }
}