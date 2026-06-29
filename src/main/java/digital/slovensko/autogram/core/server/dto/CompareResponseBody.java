package digital.slovensko.autogram.core.server.dto;

import java.util.List;

/**
 * Result of comparing original file(s) A against a signed file B.
 *
 * @param contentMatches          true when every payload document of A was found unchanged inside B
 * @param allSignaturesPreserved  true when B contains all signatures that were present on A
 * @param matchedDocuments        pairs of original/signed documents that were matched by content
 * @param unmatchedOriginals      original payload documents that were not found inside B
 * @param extraSignedDocuments    payload documents present in B that did not match any original
 * @param preservedSignatures     signatures of A that are also present on B
 * @param missingSignatures       signatures of A that are missing from B
 * @param newSignatures           signatures present on B that were not on A
 */
public record CompareResponseBody(
        boolean contentMatches,
        boolean allSignaturesPreserved,
        List<DocumentMatch> matchedDocuments,
        List<DocumentRef> unmatchedOriginals,
        List<DocumentRef> extraSignedDocuments,
        List<SignatureRef> preservedSignatures,
        List<SignatureRef> missingSignatures,
        List<SignatureRef> newSignatures) {

    public record DocumentMatch(String originalFilename, String signedFilename) {
    }

    public record DocumentRef(String filename, String mimeType) {
    }

    public record SignatureRef(String signedBy, String signingTime) {
    }
}
