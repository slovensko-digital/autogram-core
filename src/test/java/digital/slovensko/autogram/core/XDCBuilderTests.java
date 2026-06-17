package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import digital.slovensko.autogram.core.eforms.xdc.XDCBuilder;
import eu.europa.esig.dss.enumerations.*;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XDCBuilderTests {
    @Test
    void testTransformsPlainHtmlWithoutAddingNamespaces() throws IOException {
        var transformation = new String(this.getClass().getResourceAsStream("general_agenda.xslt").readAllBytes(), StandardCharsets.UTF_8);
        var xsdSchema = new String(this.getClass().getResourceAsStream("general_agenda.xsd").readAllBytes(), StandardCharsets.UTF_8);

        var document = new InMemoryDocument(this.getClass().getResourceAsStream("general_agenda.xml").readAllBytes(), "general_agenda.xml", MimeTypeEnum.XML);

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
                    "http://data.gov.sk/doc/eform/App.GeneralAgenda/1.9",
                    transformation,
                    xsdSchema,
                    "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                    null,
                    null,
                    false),
            false,
            null,
            false,
            800,
            document);

        var out = XDCBuilder.transform(params, document.getName(), EFormUtils.getXmlFromDocument(document));
        var transformed = new String(out.openStream().readAllBytes(), StandardCharsets.UTF_8);

        var expected = new String(this.getClass().getResourceAsStream("general_agenda_xdc.xml").readAllBytes(), StandardCharsets.UTF_8);

        // couldn't find a way to compare XMLs without the newline at the end
        // delete \n or \r\n from the end of the string
        expected = expected.replaceAll("\\r\\n$|\\n$", "");
        assertEquals(expected, transformed);
    }

    @Test
    void testTrustedXsltWithInternalDoctypeCanBeReferenced() throws IOException {
        var transformation = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE xsl:stylesheet [
                    <!ENTITY copy "&#169;">
                ]>
                <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                    <xsl:output method="text" omit-xml-declaration="yes" />
                    <xsl:template match="/">Ahoj &copy;</xsl:template>
                </xsl:stylesheet>
                """;
        var xsdSchema = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                    <xs:element name="root" type="xs:string"/>
                </xs:schema>
                """;

        var document = new InMemoryDocument("<root/>".getBytes(StandardCharsets.UTF_8), "root.xml", MimeTypeEnum.XML);

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
                        "http://data.gov.sk/doc/eform/Test/1.0",
                        transformation,
                        xsdSchema,
                        "http://data.gov.sk/def/container/xmldatacontainer+xml/1.1",
                        null,
                        new XsltParams(null, null, "TXT", null, null, true),
                        false),
                false,
                null,
                false,
                800,
                document);

        var out = XDCBuilder.transform(params, document.getName(), EFormUtils.getXmlFromDocument(document));
        var transformed = new String(out.openStream().readAllBytes(), StandardCharsets.UTF_8);

        org.junit.jupiter.api.Assertions.assertTrue(transformed.contains("UsedPresentationSchemaReference"));
    }
}
