package digital.slovensko.autogram.core.visualization;

import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningParameters;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class DocumentVisualizationBuilderTests {
//        TODO
//        @Test
//        void testSigningJobTransformToHtml() throws IOException, ParserConfigurationException, SAXException {
//                var transformation = new String(this.getClass().getResourceAsStream("PovolenieZdravotnictvo.html.xslt").readAllBytes());
//                var document = new InMemoryDocument(this.getClass().getResourceAsStream("rozhodnutie_X4564-2.xml"),"rozhodnutie_X4564-2.xml");
//
//                var params = SigningParameters.buildParameters(
//                        SignatureLevel.XAdES_BASELINE_B,
//                        DigestAlgorithm.SHA256,
//                        ASiCContainerType.ASiC_E,
//                        SignaturePackaging.ENVELOPING,
//                        false,
//                        CanonicalizationMethod.INCLUSIVE,
//                        CanonicalizationMethod.INCLUSIVE,
//                        CanonicalizationMethod.INCLUSIVE,
//                        new EFormAttributes(
//                                "id1/asa",
//                                transformation,
//                                null,
//                                null,
//                                null,
//                                null,
//                                false),
//                        false,
//                        null,
//                        false,
//                        800,
//                        document,
//                        null,
//                        true);
//
//                SigningJob job = SigningJob.buildFromRequest(document, params, null);
//
//                var visualizedDocument = DocumentVisualizationBuilder.fromJob(job);
//                if (visualizedDocument instanceof HTMLVisualization d) {
//                        var html = d.getDocumentString();
//                        assertFalse(html.isEmpty());
//                } else {
//                        if (visualizedDocument != null)
//                                fail("Expected HTMLVisualizedDocument but got"
//                                        + visualizedDocument.getClass().getName());
//                        fail("Expected HTMLVisualizedDocument but got null");
//                }
//        }
//        // @Test
//        // void testSigningJobTransformFo() throws IOException,
//        // ParserConfigurationException,
//        // SAXException, TransformerException {
//        // var transformation = new String(this.getClass()
//        // .getResourceAsStream(
//        // "PovolenieZdravotnictvo.fo.xsl")
//        // .readAllBytes());
//
//        // var document = new InMemoryDocument(
//        // this.getClass().getResourceAsStream(
//        // "rozhodnutie_X4564-2.xml"),
//        // "rozhodnutie_X4564-2.xml");
//
//        // var params = new SigningParameters(SignatureLevel.XAdES_BASELINE_B,
//        // ASiCContainerType.ASiC_E, null, SignaturePackaging.ENVELOPING,
//        // DigestAlgorithm.SHA256, false, CanonicalizationMethod.INCLUSIVE,
//        // CanonicalizationMethod.INCLUSIVE, CanonicalizationMethod.INCLUSIVE,
//        // null, transformation, "id1/asa", false, 800);
//
//        // SigningJob job = new SigningJob(document, params, dummyResponder);
//
//        // var xml = DocumentVisualization.fromJob(job).getVisualizedDocument();
//        // assertFalse(xml.isEmpty());
//        // }
//
//        @Test
//        void testSigningJobTransformSb() throws IOException, ParserConfigurationException, SAXException {
//                var transformation = new String(this.getClass().getResourceAsStream("PovolenieZdravotnictvo.sb.xslt").readAllBytes());
//                var document = new InMemoryDocument(this.getClass().getResourceAsStream("rozhodnutie_X4564-2.xml"), "rozhodnutie_X4564-2.xml");
//
//                var params = SigningParameters.buildParameters(
//                    SignatureLevel.XAdES_BASELINE_B,
//                    DigestAlgorithm.SHA256,
//                    ASiCContainerType.ASiC_E,
//                    SignaturePackaging.ENVELOPING,
//                    false,
//                    CanonicalizationMethod.INCLUSIVE,
//                    CanonicalizationMethod.INCLUSIVE,
//                    CanonicalizationMethod.INCLUSIVE,
//                    new EFormAttributes(
//                        "id1/asa",
//                        transformation,
//                        null,
//                        null,
//                        null,
//                        null,
//                        false),
//                    false,
//                    null,
//                    false,
//                    800,
//                    document,
//                    null,
//                    false);
//
//                SigningJob job = SigningJob.buildFromRequest(document, params, null);
//
//                var visualizedDocument = DocumentVisualizationBuilder.fromJob(job);
//                if (visualizedDocument instanceof HTMLVisualization d) {
//                        var html = d.getDocumentString();
//                        assertFalse(html.isEmpty());
//                } else {
//                        fail("Expected HTMLVisualizedDocument");
//                }
//        }
}
