package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class DocumentVisualizationBuilderTests {
    private DSSDocument document = new InMemoryDocument(this.getClass().getResourceAsStream("rozhodnutie_X4564-2.xml"), "rozhodnutie_X4564-2.xml");
    private String schema;

    public DocumentVisualizationBuilderTests() throws IOException {
        schema = new String(this.getClass().getResourceAsStream("rozhodnutie_X4564-2.xsd").readAllBytes());
    }

    @Test
    void testSigningJobTransformToHtml() throws IOException, ParserConfigurationException, SAXException {
        var transformation = new String(this.getClass().getResourceAsStream("PovolenieZdravotnictvo.html.xslt").readAllBytes());

        var params = SigningParameters.buildParameters(
                SignatureLevel.XAdES_BASELINE_B,
                DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E,
                SignaturePackaging.ENVELOPING,
                false,
                CanonicalizationMethod.INCLUSIVE,
                CanonicalizationMethod.INCLUSIVE,
                CanonicalizationMethod.INCLUSIVE,
                new EFormAttributes(
                        "id1/asa",
                        transformation,
                        schema,
                        "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                        null,
                        null,
                        false),
                false,
                null,
                false,
                800,
                document);

        SigningJob job = SigningJob.buildFromRequest(document, params, null);

        var visualizedDocument = DocumentVisualizationBuilder.fromJob(job);
        if (visualizedDocument instanceof HTMLVisualization d) {
            var html = d.getDocumentString();
            assertFalse(html.isEmpty());
        } else {
            if (visualizedDocument != null)
                fail("Expected HTMLVisualizedDocument but got"
                        + visualizedDocument.getClass().getName());
            fail("Expected HTMLVisualizedDocument but got null");
        }
    }

    @Test
    void testSigningJobTransformSb() throws IOException, ParserConfigurationException, SAXException {
        var transformation = new String(this.getClass().getResourceAsStream("PovolenieZdravotnictvo.sb.xslt").readAllBytes());

        var params = SigningParameters.buildParameters(
                SignatureLevel.XAdES_BASELINE_B,
                DigestAlgorithm.SHA256,
                ASiCContainerType.ASiC_E,
                SignaturePackaging.ENVELOPING,
                false,
                CanonicalizationMethod.INCLUSIVE,
                CanonicalizationMethod.INCLUSIVE,
                CanonicalizationMethod.INCLUSIVE,
                new EFormAttributes(
                        "id1/asa",
                        transformation,
                        schema,
                        "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                        null,
                        null,
                        false),
                false,
                null,
                false,
                800,
                document);

        SigningJob job = SigningJob.buildFromRequest(document, params, null);

        var visualizedDocument = DocumentVisualizationBuilder.fromJob(job);
        if (visualizedDocument instanceof HTMLVisualization d) {
            var html = d.getDocumentString();
            assertFalse(html.isEmpty());
        } else {
            fail("Expected HTMLVisualizedDocument");
        }
    }
}
