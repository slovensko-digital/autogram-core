package digital.slovensko.autogram.core.server.dto;

import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.signature.AdvancedSignature;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidationResponseBodyTimestampTests {
    private static AdvancedSignature loadSignature(String resource) throws Exception {
        try (var stream = ValidationResponseBodyTimestampTests.class.getResourceAsStream("/digital/slovensko/autogram/core/validation/" + resource)) {
            var document = new InMemoryDocument(stream.readAllBytes(), resource);
            var validator = SignedDocumentValidator.fromDocument(document);
            validator.setCertificateVerifier(new eu.europa.esig.dss.spi.validation.CommonCertificateVerifier());
            return validator.getSignatures().get(0);
        }
    }

    @Test
    void padesDocumentTimestampAfterSignatureCoversSignature() throws Exception {
        var signature = loadSignature("pades_signed_document_timestamped.pdf");

        Assertions.assertEquals(1, signature.getDocumentTimestamps().size());
        Assertions.assertTrue(ValidationResponseBody.coversSignature(signature.getDocumentTimestamps().get(0), signature));
    }

    @Test
    void padesWithoutTimestampHasNoDocumentTimestamps() throws Exception {
        var signature = loadSignature("pades_signed.pdf");

        Assertions.assertTrue(signature.getDocumentTimestamps().isEmpty());
    }
}
