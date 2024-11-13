package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import eu.europa.esig.dss.model.DSSDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class XDCValidatorTests {
    @ParameterizedTest
    @MethodSource({"digital.slovensko.autogram.core.TestMethodSources#xdcDocumentsProvider",
            "digital.slovensko.autogram.core.TestMethodSources#xdcDocumentsWithXmlMimetypeProvider"})
    void testReturnsTrueForAllXDCsRegerdlessOfMimeType(DSSDocument document) {
        Assertions.assertTrue(XDCValidator.isXDCContent(document));
    }

    @ParameterizedTest
    @MethodSource({"digital.slovensko.autogram.core.TestMethodSources#nonXdcXmlDocumentsProvider"})
    void testReturnsFalseForAllNonXDCsRegerdlessOfMimeType(DSSDocument document) {
        Assertions.assertFalse(XDCValidator.isXDCContent(document));
    }
}
