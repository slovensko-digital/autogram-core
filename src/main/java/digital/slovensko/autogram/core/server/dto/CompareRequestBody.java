package digital.slovensko.autogram.core.server.dto;

import java.util.Base64;
import java.util.List;

import digital.slovensko.autogram.core.AutogramMimeType;
import digital.slovensko.autogram.core.server.errors.MalformedBodyException;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;

/**
 * Request body of the signed-version comparison endpoint.
 *
 * @param originalDocuments the original file(s) A - may be unsigned input or an already signed file
 * @param signedDocument    the signed file B that should be a signed version of A
 */
public record CompareRequestBody(List<Document> originalDocuments, Document signedDocument) {

    public void validate() throws MalformedBodyException {
        if (originalDocuments == null || originalDocuments.isEmpty())
            throw new MalformedBodyException("Missing original documents", "originalDocuments is null or empty");

        for (var document : originalDocuments)
            if (document == null || document.content() == null)
                throw new MalformedBodyException("Invalid original document", "Original document content is null");

        if (signedDocument == null || signedDocument.content() == null)
            throw new MalformedBodyException("Missing signed document", "signedDocument content is null");
    }

    public List<DSSDocument> getOriginalDSSDocuments() throws MalformedBodyException {
        return originalDocuments.stream().map(CompareRequestBody::toDSSDocument).toList();
    }

    public DSSDocument getSignedDSSDocument() throws MalformedBodyException {
        return toDSSDocument(signedDocument);
    }

    private static DSSDocument toDSSDocument(Document document) throws MalformedBodyException {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(document.content());
        } catch (IllegalArgumentException e) {
            throw new MalformedBodyException("Invalid Base64 content", e);
        }

        var dssDocument = new InMemoryDocument(bytes, document.filename());
        var mimeType = parseMimeType(document.mimeType());
        if (mimeType != null)
            dssDocument.setMimeType(mimeType);

        return dssDocument;
    }

    private static MimeType parseMimeType(String mimeType) {
        if (mimeType == null)
            return null;

        var normalized = mimeType.replace(";base64", "").replace("; base64", "").trim();
        if (normalized.isBlank())
            return null;

        return AutogramMimeType.fromMimeTypeString(normalized);
    }
}
