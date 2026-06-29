package digital.slovensko.autogram.core.validation;

import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

class SignedVersionValidatorTest {

    private DSSDocument resource(String name, eu.europa.esig.dss.enumerations.MimeType mimeType) throws IOException {
        try (var stream = SignedVersionValidatorTest.class.getResourceAsStream("/digital/slovensko/autogram/core/" + name)) {
            return new InMemoryDocument(stream.readAllBytes(), name, mimeType);
        }
    }

    @Test
    void plainXmlMatchesSignedXdcContainer() throws IOException {
        var original = resource("general_agenda.xml", MimeTypeEnum.XML);
        var signed = resource("general_agenda.asice", MimeTypeEnum.ASICE);

        var result = SignedVersionValidator.compare(List.of(original), signed);

        Assertions.assertTrue(result.contentMatches());
        Assertions.assertTrue(result.allSignaturesPreserved());
        Assertions.assertTrue(result.missingSignatures().isEmpty());
        Assertions.assertFalse(result.newSignatures().isEmpty());
    }

    @Test
    void tamperedXmlDoesNotMatch() throws IOException {
        var tamperedXml = new String(
                SignedVersionValidatorTest.class.getResourceAsStream("/digital/slovensko/autogram/core/general_agenda.xml").readAllBytes(),
                StandardCharsets.UTF_8).replace("Testovacie podanie", "Tampered subject");
        var original = new InMemoryDocument(tamperedXml.getBytes(StandardCharsets.UTF_8), "general_agenda.xml", MimeTypeEnum.XML);
        var signed = resource("general_agenda.asice", MimeTypeEnum.ASICE);

        var result = SignedVersionValidator.compare(List.of(original), signed);

        Assertions.assertFalse(result.contentMatches());
        Assertions.assertFalse(result.unmatchedOriginals().isEmpty());
    }

    @Test
    void binaryPngMatchesSignedContainer() throws IOException {
        var original = resource("sample.png", MimeTypeEnum.PNG);
        var signed = resource("sample_png_xades.asice", MimeTypeEnum.ASICE);

        var result = SignedVersionValidator.compare(List.of(original), signed);

        Assertions.assertTrue(result.contentMatches());
        Assertions.assertFalse(result.newSignatures().isEmpty());
    }

    @Test
    void pdfInsideXadesContainerMatches() throws IOException {
        var original = resource("sample.pdf", MimeTypeEnum.PDF);
        var signed = resource("sample_pdf_xades.asice", MimeTypeEnum.ASICE);

        var result = SignedVersionValidator.compare(List.of(original), signed);

        Assertions.assertTrue(result.contentMatches());
    }

    @Test
    void pdfInsideCadesContainerMatches() throws IOException {
        var original = resource("sample.pdf", MimeTypeEnum.PDF);
        var signed = resource("sample_pdf_cades.asice", MimeTypeEnum.ASICE);

        var result = SignedVersionValidator.compare(List.of(original), signed);

        Assertions.assertTrue(result.contentMatches());
    }

    @Test
    void unsignedSignedDocumentThrows() throws IOException {
        var original = resource("sample.pdf", MimeTypeEnum.PDF);
        var notSigned = resource("sample.pdf", MimeTypeEnum.PDF);

        Assertions.assertThrows(DocumentNotSignedYetException.class,
                () -> SignedVersionValidator.compare(List.of(original), notSigned));
    }

    @Test
    void alreadySignedOriginalAgainstItselfPreservesSignatures() throws IOException {
        var original = resource("general_agenda.asice", MimeTypeEnum.ASICE);
        var signed = resource("general_agenda.asice", MimeTypeEnum.ASICE);

        var result = SignedVersionValidator.compare(List.of(original), signed);

        Assertions.assertTrue(result.contentMatches());
        Assertions.assertTrue(result.allSignaturesPreserved());
        Assertions.assertTrue(result.missingSignatures().isEmpty());
        Assertions.assertTrue(result.newSignatures().isEmpty());
        Assertions.assertFalse(result.preservedSignatures().isEmpty());
    }

    @Test
    void missingSignatureIsDetected() throws IOException {
        // A is a signed file; B is an unrelated signed file that does not carry A's signature.
        var original = resource("general_agenda.asice", MimeTypeEnum.ASICE);
        var unrelatedSigned = resource("sample_png_xades.asice", MimeTypeEnum.ASICE);

        var result = SignedVersionValidator.compare(List.of(original), unrelatedSigned);

        Assertions.assertFalse(result.allSignaturesPreserved());
        Assertions.assertFalse(result.missingSignatures().isEmpty());
    }
}
