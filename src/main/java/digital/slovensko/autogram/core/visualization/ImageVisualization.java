package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.util.Base64;

public class ImageVisualization extends Visualization {
    private final DSSDocument document;

    public ImageVisualization(DSSDocument document, SigningJob job) {
        super(job);
        this.document = document;
    }

    @Override
    public void initialize(Visualizer visualizer) {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showImageVisualization(document);
    }

    @Override
    public DSSDocument getDocument() {
        try {
            var content = Base64.getEncoder().encode(document.openStream().readAllBytes());
            var temp = "<img style=\" width: 100%; height: 100%; object-fit: contain;\" src=\"data:" + document.getMimeType().getMimeTypeString() + ";base64," + new String(content) + "\" />";

            return new InMemoryDocument(temp.getBytes(), document.getName(), MimeTypeEnum.HTML);

        } catch (Exception e) {
            return null;
        }
    }
}