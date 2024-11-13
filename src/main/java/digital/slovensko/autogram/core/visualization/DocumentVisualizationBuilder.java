package digital.slovensko.autogram.core.visualization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.ParserConfigurationException;
import eu.europa.esig.dss.model.DSSDocument;
import org.xml.sax.SAXException;
import static digital.slovensko.autogram.core.AutogramMimeType.*;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.util.AsicContainerUtils;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public class DocumentVisualizationBuilder {

    private final DSSDocument document;
    private final SigningParameters parameters;

    private DocumentVisualizationBuilder(DSSDocument document, SigningParameters parameters) {
        this.document = document;
        this.parameters = parameters;
    }

    public static Visualization fromJob(SigningJob job) {
        return new DocumentVisualizationBuilder(job.getDocument(), job.getParameters()).build(job);
    }

    private Visualization build(SigningJob job) {
        try {
            return createVisualization(job);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            return null;
        }
    }

    private Visualization createVisualization(SigningJob job) throws IOException, ParserConfigurationException, SAXException {
        var documentToDisplay = document;
        if (isAsice(documentToDisplay.getMimeType())) {
            try {
                documentToDisplay = AsicContainerUtils.getOriginalDocument(document);
            } catch (AutogramException e) {
                return null;
            }
        }

        var transformation = parameters.getTransformation();

        if (isDocumentSupportingTransformation(documentToDisplay) && isTranformationAvailable(transformation)) {
            var transformationOutputMimeType = parameters.getXsltDestinationType();

            if ("HTML".equals(transformationOutputMimeType) || "XHTML".equals(transformationOutputMimeType))
                return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), documentToDisplay.getName(), job);

            if (transformationOutputMimeType.equals("TXT"))
                return new PlainTextVisualization(EFormUtils.transform(documentToDisplay, transformation), documentToDisplay.getName(), job);

            return null;
        }

        if (documentToDisplay.getMimeType().equals(MimeTypeEnum.HTML))
            return new HTMLVisualization(EFormUtils.transform(documentToDisplay, transformation), documentToDisplay.getName(), job);

        if (isTxt(documentToDisplay.getMimeType()))
            return new PlainTextVisualization(new String(documentToDisplay.openStream().readAllBytes(), StandardCharsets.UTF_8), documentToDisplay.getName(), job);

        if (isPDF(documentToDisplay.getMimeType()))
            return new PDFVisualization(documentToDisplay, job);

        if (isImage(documentToDisplay.getMimeType()))
            return new ImageVisualization(documentToDisplay, job);

        return new UnsupportedVisualization(job);
    }

    private boolean isTranformationAvailable(String transformation) {
        return transformation != null;
    }

    private boolean isDocumentSupportingTransformation(DSSDocument document) {
        return isXDC(document.getMimeType()) || isXML(document.getMimeType());
    }
}
