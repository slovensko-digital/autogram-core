package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.*;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SigningParametersTests {
    private String inclusive;
    private ASiCContainerType asice;
    private SignaturePackaging enveloping;
    private String xdcXmlns;

    private byte[] generalAgendaXml;
    private String xsdSchema;
    private String xsltTransformation;
    private String identifier;
    private TSPSource tspSource;
    private EFormAttributes attributes;

    @BeforeAll
    void setDefaultValues() throws IOException {
        inclusive = CanonicalizationMethod.INCLUSIVE;
        asice = ASiCContainerType.ASiC_E;
        enveloping = SignaturePackaging.ENVELOPING;
        xdcXmlns = "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1";

        generalAgendaXml = this.getClass().getResourceAsStream("general_agenda.xml").readAllBytes();
        xsdSchema = new String(this.getClass().getResourceAsStream("general_agenda.xsd").readAllBytes(), StandardCharsets.UTF_8);
        xsltTransformation = new String(
                this.getClass().getResourceAsStream("general_agenda.xslt").readAllBytes(), StandardCharsets.UTF_8);
        identifier = "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9";
        tspSource = null;

        attributes = new EFormAttributes("id1/asa", null, null, xdcXmlns, null, null, false);
    }

    @Test
    void testThrowsAutogramExceptionWhenNoMimeType() throws IOException {
        var document = new InMemoryDocument(generalAgendaXml);

        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, attributes, false, null, false, 800, document));
    }

    @Test
    void testThrowsAutogramExceptionWhenNoDocument() {
        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, attributes, false, null, false, 800, null));
    }

    @Test
    void testThrowsXMLValidationFailedWhenNoXML() {
        var document = new InMemoryDocument("not xml".getBytes(), "doc.xml", MimeTypeEnum.XML);

        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document));
    }

    @Test
    void testThrowsAutogramExceptionWhenNoSignatureLevel() {
        var document = new InMemoryDocument(generalAgendaXml, "doc.xml", MimeTypeEnum.XML);

        Assertions.assertThrows(SigningParametersException.class,
                () -> SigningParameters.buildParameters(null, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, attributes, false, null, false, 800, null));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesNoConatiner(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null,
                        false, null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesInAsice(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, null, null, null, null,
                        false, null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesXdcInAsiceWith(DSSDocument document) {
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#generalAgendaProvider")
    void testDoesNotThrowWithMinimalParametersForXadesXdcInAsiceAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#invalidXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXml(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#invalidXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlWithAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#nonEFormXmlProvider")
    void testThrowsUnknownEformExceptionWithInvalidXmlEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        var signingParameters = SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document);

        Assertions.assertTrue(signingParameters.isPlainXml());
    }


    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#xsdSchemaFailedValidationXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlSchema(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#xsdSchemaFailedValidationXmlProvider")
    void testThrowsAutogramExceptionWithInvalidXmlSchemaWithAutoLoadEform(DSSDocument document) {
        // TODO: mock eform S3 resource
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#generalAgendaProvider")
    void testThrowsAutogramExceptionWithUnknownEformXml(DSSDocument document) {
        Assertions.assertThrows(EFormException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, null, null, null, new EFormAttributes(null, null, null,
                        xdcXmlns, null, null, false), false,
                        null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#unknownEfomXmlProvider")
    void testThrowsAutogramExceptionWithUnknownEformXmlWithAutoLoadEform(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#mismatchedDigestsXmlProvider")
    void testThrowsAutogramExceptionWithMismatchedDigestsXml(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, xsltTransformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource({ "digital.slovensko.autogram.core.TestMethodSources#mismatchedDigestsXmlProvider",
            "digital.slovensko.autogram.core.TestMethodSources#mismatchedDigestsFSXmlProvider"})
    void testThrowsAutogramExceptionWithMismatchedDigestsXmlWithAutoLoadEform(DSSDocument document) {
        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, null, null,
                        false, null, null, null, null, true,
                        "792_772", false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.core.TestMethodSources#invalidAsiceProvider")
    void testThrowsOriginalDocumentNotFoundWithAsiceWithoutSignature(DSSDocument document) throws IOException {
        Assertions.assertThrows(OriginalDocumentNotFoundException.class,
                () -> SigningParameters.buildForASiCWithXAdES(document, false, false, BaselineLevel.B, false));
    }

    @Test
    void testThrowsExceptionWithAsiceWithEmptyXml() throws IOException {
        var document = new InMemoryDocument(
                this.getClass().getResourceAsStream("empty_xml.asice").readAllBytes(),
                "empty_xml.asice");

        Assertions.assertThrows(XMLValidationException.class,
                () -> SigningParameters.buildForASiCWithXAdES(document, false, false, BaselineLevel.B, false));
    }

    @Test
    void testInvalidTransformation() throws IOException {
        var generalAgendaXml = this.getClass().getResourceAsStream("general_agenda.xml").readAllBytes();
        var document = new InMemoryDocument(generalAgendaXml, "doc.xml", MimeTypeEnum.XML);

        var transformation = "invalid transformation";
        Assertions.assertThrows(TransformationParsingErrorException.class,
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, null,
                        false, inclusive, inclusive, inclusive, new EFormAttributes(identifier, transformation, xsdSchema,
                        xdcXmlns, null, null, false), false, null, false, 800, document));
    }

    @ParameterizedTest
    @MethodSource("digital.slovensko.autogram.TestMethodSources#orsrDocumentsProvider")
    void testDoesNotThrowWithEmbeddedXdcWithoutAutoLoad(DSSDocument document) {
        var eFormAttributes = new EFormAttributes(identifier, "", "", "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", null, null, true);

        Assertions.assertDoesNotThrow(
                () -> SigningParameters.buildParameters(SignatureLevel.XAdES_BASELINE_B, null, asice, enveloping,
                        false, inclusive, inclusive, inclusive, eFormAttributes, false, null, false, 800, document));
    }
}
