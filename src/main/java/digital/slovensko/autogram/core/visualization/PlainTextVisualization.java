package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

public class PlainTextVisualization extends Visualization {
    private final String content;
    private final String name;

    public PlainTextVisualization(String plainText, String name, SigningJob job) {
        super(job);
        this.content = plainText;
        this.name = name;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showPlainTextVisualization(content);
    }

    @Override
    public DSSDocument getDocument() {
        return new InMemoryDocument(content.getBytes(), name, MimeTypeEnum.TEXT);
    }
}