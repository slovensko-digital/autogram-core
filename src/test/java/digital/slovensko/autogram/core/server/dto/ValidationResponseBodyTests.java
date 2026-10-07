package digital.slovensko.autogram.core.server.dto;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ValidationResponseBodyTests {
    @Test
    void extractAgpMetadataPrefersClaimedRoles() {
        var metadata = ValidationResponseBody.extractAgpMetadata(
                List.of("AGP-REF:PUBLIC-REF-123", "AGP-HOST:agp.example.test"),
                "AGP-REF:ignored|AGP-HOST:ignored.example.test");

        Assertions.assertEquals("PUBLIC-REF-123", metadata.agpReference());
        Assertions.assertEquals("agp.example.test", metadata.agpInstance());
    }

    @Test
    void extractAgpMetadataFallsBackToContentIdentifier() {
        var metadata = ValidationResponseBody.extractAgpMetadata(
                List.of(),
                "AGP-REF:PUBLIC-REF-123|AGP-HOST:agp.example.test");

        Assertions.assertEquals("PUBLIC-REF-123", metadata.agpReference());
        Assertions.assertEquals("agp.example.test", metadata.agpInstance());
    }

    @Test
    void extractAgpMetadataReturnsNullsWhenMissing() {
        var metadata = ValidationResponseBody.extractAgpMetadata(
                List.of("OTHER:VALUE"),
                null);

        Assertions.assertNull(metadata.agpReference());
        Assertions.assertNull(metadata.agpInstance());
    }

    @Test
    void buildHandlesEnvelopingCadesWithoutContainer() throws Exception {
        try (var stream = getClass().getResourceAsStream("/digital/slovensko/autogram/core/sample_pdf_cades_enveloping.p7m")) {
            var document = new InMemoryDocument(stream.readAllBytes());
            var validator = SignedDocumentValidator.fromDocument(document);
            validator.setCertificateVerifier(new CommonCertificateVerifier());

            var body = ValidationResponseBody.build(validator.validateDocument(), validator, document);

            Assertions.assertNull(body.containerType());
            Assertions.assertEquals("CAdES", body.signatureForm());
            Assertions.assertEquals(1, body.signatures().size());
            Assertions.assertEquals(1, body.signedObjects().size());
            Assertions.assertEquals(body.signatures().get(0).signedObjectsIds(), List.of(body.signedObjects().get(0).id()));
            Assertions.assertEquals(MimeTypeEnum.BINARY.getMimeTypeString(), body.signedObjects().get(0).mimeType());
            Assertions.assertNull(body.unsignedObjects());
        }
    }
}