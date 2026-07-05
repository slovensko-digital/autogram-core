package digital.slovensko.autogram.core.server.dto;

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
}