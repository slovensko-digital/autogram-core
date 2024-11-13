package digital.slovensko.autogram.core.server.dto;

import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.server.errors.MalformedBodyException;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.io.IOException;
import java.util.Base64;

public record Document(String filename, String content, String mimeType) {
    public Document(String content) {
        this("document", content);
    }

    public Document(String filename, String content) {
        this(filename, content, "");
    }

    public static Object buildFromDSS(DSSDocument document) {
        try (var stream = document.openStream()) {
            return new Document(document.getName(), new String(Base64.getEncoder().encode(stream.readAllBytes())), document.getMimeType().getMimeTypeString() + ";base64");
        } catch (IOException e) {
            throw new UnrecognizedException(e);
        }
    }

    public InMemoryDocument getDecodedContent() throws MalformedBodyException {
        try {
            return new InMemoryDocument(Base64.getDecoder().decode(content));
        } catch (IllegalArgumentException e) {
            throw new MalformedBodyException("Invalid Base64 content", e);
        }
    }
}
