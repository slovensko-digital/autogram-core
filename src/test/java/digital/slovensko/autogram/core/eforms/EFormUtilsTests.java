package digital.slovensko.autogram.core.eforms;

import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import digital.slovensko.autogram.core.errors.TransformationException;
import digital.slovensko.autogram.core.errors.TransformationParsingErrorException;
import digital.slovensko.autogram.core.errors.XMLValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.nio.charset.StandardCharsets;


public class EFormUtilsTests {
    @ParameterizedTest
    @CsvSource({
            "dic_fs792_772_exte.xml,792_772",
            "dic_fs792_772_.xml,792_772",
            "dic_fs792_772.xml,792_772",
            "dic_fs792_772exte.xml,792_772",
            "dic2120515056_fs792_772_indented.xml,792_772",
            "dic2120515056_fs792_772indented.xml,792_772",
            "dic2120515056_fs792_772.xml,792_772",
            "dic_fs2682_712__idk.xml,2682_712",
            "dic_fs2682_712idk.xml,2682_712",
            "dic_fs2682_712_idk.xml,2682_712",
            "dic2120515056_fs792_772__1__0.xml,792_772",
            "invalid.xml,"
    })
    void testGetFsFormIdFromFilename(String filename, String expected) {
        Assertions.assertEquals(expected, EFormUtils.getFsFormIdFromFilename(filename));
    }

    @ParameterizedTest
    @CsvSource({
            "792_772,792_772"
    })
    void testTranslateFsFormId(String fsFormId, String expected) {
        Assertions.assertEquals(expected, EFormUtils.translateFsFormId(fsFormId));
    }

    @ParameterizedTest
    @CsvSource({
        "http://www.justice.gov.sk/Forms,true",
        "http://www.justice.gov.sk/Forms http://eformulare.justice.sk/path/form.xsd,true",
        "http://eformulare.justice.sk/form,true",
        "http://evil.com/form http://www.justice.gov.sk/form.xsd,false",
        "http://evil.com/?x=justice.gov.sk/Forms,false",
        "http://httpbin.org/forms,false",
    })
    void testIsOrsrUri(String uri, boolean expected) {
        Assertions.assertEquals(expected, EFormUtils.isOrsrUri(uri));
    }

    @Test
    void testIsOrsrUriReturnsFalseForNull() {
        Assertions.assertFalse(EFormUtils.isOrsrUri(null));
    }

    @Test
    void testExtractTransformationOutputMimeTypeStringAllowsInternalDoctype() {
        var transformation = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE xsl:stylesheet [
                    <!ENTITY nbsp "&#160;">
                ]>
                <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                    <xsl:output method="text" />
                    <xsl:template match="/">Ahoj&nbsp;svet</xsl:template>
                </xsl:stylesheet>
                """;

        Assertions.assertThrows(TransformationParsingErrorException.class,
            () -> EFormUtils.extractTransformationOutputMimeTypeString(transformation));
        Assertions.assertEquals("TXT", EFormUtils.extractTransformationOutputMimeTypeString(transformation, true));
    }

    @Test
    void testTransformAllowsInternalDoctypeInXslt() {
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

        var document = new InMemoryDocument("<root/>".getBytes(StandardCharsets.UTF_8), "test.xml");

        Assertions.assertThrows(TransformationException.class, () -> EFormUtils.transform(document, transformation));
        Assertions.assertEquals("Ahoj ©", EFormUtils.transform(document, transformation, true));
    }

    @Test
    void testComputeDigestAllowsInternalDoctypeOnlyForTrustedXslt() {
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

        Assertions.assertThrows(XMLValidationException.class,
                () -> EFormUtils.computeDigest(transformation.getBytes(StandardCharsets.UTF_8),
                        CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, StandardCharsets.UTF_8));

        var digest = EFormUtils.computeDigest(transformation.getBytes(StandardCharsets.UTF_8),
                CanonicalizationMethod.INCLUSIVE, DigestAlgorithm.SHA256, StandardCharsets.UTF_8, true);

        Assertions.assertFalse(digest.isEmpty());
    }
}