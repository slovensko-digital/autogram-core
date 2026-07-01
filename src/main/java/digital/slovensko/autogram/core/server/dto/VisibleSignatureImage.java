package digital.slovensko.autogram.core.server.dto;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.server.errors.MalformedBodyException;
import digital.slovensko.autogram.core.server.errors.RequestValidationException;
import eu.europa.esig.dss.model.InMemoryDocument;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class VisibleSignatureImage {
    private String filename;
    private String content;
    private String mimeType;

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void validate(String labelPrefix) throws RequestValidationException {
        if (content == null || content.isBlank())
            throw new RequestValidationException(labelPrefix + ".Content is required", "");

        if (mimeType == null || mimeType.isBlank())
            throw new RequestValidationException(labelPrefix + ".MimeType is required", "");
    }

    public InMemoryDocument toDssDocument() {
        return new InMemoryDocument(decodeContent(), filename, AutogramMimeType.fromMimeTypeString(getRawMimeType()));
    }

    private String getRawMimeType() {
        return mimeType.split(";")[0];
    }

    private boolean isBase64() {
        return mimeType.contains("base64");
    }

    private byte[] decodeContent() throws MalformedBodyException {
        if (isBase64()) {
            try {
                return Base64.getDecoder().decode(content);
            } catch (IllegalArgumentException e) {
                throw new MalformedBodyException("Base64 decoding failed", "Invalid visible signature image content");
            }
        }

        return content.getBytes(StandardCharsets.UTF_8);
    }
}
