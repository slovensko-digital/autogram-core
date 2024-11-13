package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

public class HTMLVisualization extends Visualization {
    private final String content;
    private final String name;

    public HTMLVisualization(String html, String name, SigningJob job) {
        super(job);
        this.content = html;
        this.name = name;
    }

    public String getDocumentString() {
        return this.content;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showHTMLVisualization(content);
    }

    public DSSDocument getDocument() {
        return new InMemoryDocument(content.getBytes(), name, MimeTypeEnum.HTML);
    }
}