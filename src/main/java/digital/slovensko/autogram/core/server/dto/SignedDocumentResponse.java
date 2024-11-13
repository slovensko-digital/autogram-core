package digital.slovensko.autogram.core.server.dto;

import digital.slovensko.autogram.core.errors.UnrecognizedException;
import eu.europa.esig.dss.model.DSSDocument;

import java.io.IOException;
import java.util.Base64;

public record SignedDocumentResponse(String content, String mimeType, String filename, String signedBy, String issuedBy) {
    public static SignedDocumentResponse buildFormDSS(DSSDocument document, String signedBy, String issuedBy) {
        try (var stream = document.openStream()) {
            return new SignedDocumentResponse(
                    Base64.getEncoder().encodeToString(stream.readAllBytes()),
                    document.getMimeType().getMimeTypeString() + ";base64",
                    document.getName(),
                    signedBy,
                    issuedBy
            );
        } catch (IOException e) {
            throw new UnrecognizedException(e);
        }
    }

}
