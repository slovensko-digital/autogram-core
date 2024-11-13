package digital.slovensko.autogram.core.visualization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import digital.slovensko.autogram.core.SigningJob;
import eu.europa.esig.dss.model.DSSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;

public class PDFVisualization extends Visualization {
    private final DSSDocument document;

    public PDFVisualization(DSSDocument document, SigningJob job) {
        super(job);
        this.document = document;
    }

    private ArrayList<byte []> getPdfImages(int pdfDpi) throws IOException {
        var pdfDocument = PDDocument.load(this.document.openStream());
        var pdfRenderer = new PDFRenderer(pdfDocument);
        var divs = new ArrayList<byte[]>();
        for (int page = 0; page < pdfDocument.getNumberOfPages(); ++page) {
            var os = new ByteArrayOutputStream();
            var bim = pdfRenderer.renderImageWithDPI(page, pdfDpi, ImageType.RGB);
            ImageIO.write(bim, "png", os);
            divs.add(os.toByteArray());
        }

        pdfDocument.close();

        return divs;
    }

    @Override
    public void initialize(Visualizer visualizer) throws IOException {
        visualizer.setPrefWidth(getVisualizationWidth());
        visualizer.showPDFVisualization(getPdfImages(visualizer.getPdfDpi()));
    }

    @Override
    public DSSDocument getDocument() {
        return document;
    }
}