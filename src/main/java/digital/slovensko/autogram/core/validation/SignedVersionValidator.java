package digital.slovensko.autogram.core.validation;

import static digital.slovensko.autogram.core.AutogramMimeType.isXDC;
import static digital.slovensko.autogram.core.AutogramMimeType.isXML;

import java.security.cert.CertificateEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.xml.crypto.dsig.CanonicalizationMethod;

import digital.slovensko.autogram.core.eforms.EFormUtils;
import digital.slovensko.autogram.core.eforms.xdc.XDCValidator;
import digital.slovensko.autogram.core.errors.DocumentNotSignedYetException;
import digital.slovensko.autogram.core.server.dto.CompareResponseBody;
import digital.slovensko.autogram.core.server.dto.CompareResponseBody.DocumentMatch;
import digital.slovensko.autogram.core.server.dto.CompareResponseBody.DocumentRef;
import digital.slovensko.autogram.core.server.dto.CompareResponseBody.SignatureRef;
import digital.slovensko.autogram.core.util.DSSUtils;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.spi.signature.AdvancedSignature;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.xml.utils.XMLCanonicalizer;
import org.w3c.dom.Node;

/**
 * Compares an original input (file A, possibly multiple files, signed or not) against a signed
 * file B and decides whether B is a signed version of A. It checks that every payload document of A
 * is present (unchanged) inside B and - when A is already signed - that B preserves all of A's
 * signatures. B may carry additional, new signatures.
 */
public abstract class SignedVersionValidator {
    private static final String CANONICALIZATION_METHOD = CanonicalizationMethod.EXCLUSIVE;
    private static final DigestAlgorithm DIGEST_ALGORITHM = DigestAlgorithm.SHA256;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss Z");

    public static CompareResponseBody compare(List<DSSDocument> originalDocuments, DSSDocument signedDocument)
            throws DocumentNotSignedYetException {
        var signedValidator = buildValidator(signedDocument);
        if (signedValidator == null || signedValidator.getSignatures().isEmpty())
            throw new DocumentNotSignedYetException();

        var signedPayloads = extractPayloads(signedDocument);
        var signedSignatures = signedValidator.getSignatures();

        var originalPayloads = new ArrayList<DSSDocument>();
        var originalSignatures = new ArrayList<AdvancedSignature>();
        for (var original : originalDocuments) {
            originalPayloads.addAll(extractPayloads(original));
            originalSignatures.addAll(extractSignatures(original));
        }

        var matched = new ArrayList<DocumentMatch>();
        var unmatchedOriginals = new ArrayList<DocumentRef>();
        var remainingSigned = new ArrayList<>(signedPayloads);
        for (var original : originalPayloads) {
            DSSDocument match = null;
            for (var candidate : remainingSigned) {
                if (contentMatches(original, candidate)) {
                    match = candidate;
                    break;
                }
            }

            if (match != null) {
                matched.add(new DocumentMatch(original.getName(), match.getName()));
                remainingSigned.remove(match);
            } else {
                unmatchedOriginals.add(toRef(original));
            }
        }

        var extraSignedDocuments = remainingSigned.stream().map(SignedVersionValidator::toRef).toList();

        var originalSignatureIdentities = signatureIdentities(originalSignatures);
        var signedSignatureIdentities = signatureIdentities(signedSignatures);

        var preservedSignatures = new ArrayList<SignatureRef>();
        var missingSignatures = new ArrayList<SignatureRef>();
        for (var signature : originalSignatures) {
            if (signedSignatureIdentities.contains(identityOf(signature)))
                preservedSignatures.add(toRef(signature));
            else
                missingSignatures.add(toRef(signature));
        }

        var newSignatures = signedSignatures.stream()
                .filter(signature -> !originalSignatureIdentities.contains(identityOf(signature)))
                .map(SignedVersionValidator::toRef)
                .toList();

        var contentMatches = unmatchedOriginals.isEmpty() && !originalPayloads.isEmpty();
        var allSignaturesPreserved = missingSignatures.isEmpty();

        return new CompareResponseBody(
                contentMatches,
                allSignaturesPreserved,
                matched,
                unmatchedOriginals,
                extraSignedDocuments,
                preservedSignatures,
                missingSignatures,
                newSignatures);
    }

    private static SignedDocumentValidator buildValidator(DSSDocument document) {
        var validator = DSSUtils.createDocumentValidator(document);
        if (validator == null)
            return null;

        validator.setCertificateVerifier(new CommonCertificateVerifier());
        return validator;
    }

    /**
     * Returns the payload documents of a file. For an unsigned file the file itself is the payload.
     * For a signed file the documents covered by its signatures are returned.
     */
    private static List<DSSDocument> extractPayloads(DSSDocument document) {
        var validator = buildValidator(document);
        if (validator == null || validator.getSignatures().isEmpty())
            return List.of(document);

        var payloads = new ArrayList<DSSDocument>();
        var seenNames = new HashSet<String>();
        for (var signature : validator.getSignatures()) {
            for (var original : validator.getOriginalDocuments(signature.getId())) {
                if (seenNames.add(original.getName()))
                    payloads.add(original);
            }
        }

        return payloads.isEmpty() ? List.of(document) : payloads;
    }

    private static List<AdvancedSignature> extractSignatures(DSSDocument document) {
        var validator = buildValidator(document);
        if (validator == null)
            return List.of();

        return validator.getSignatures();
    }

    private static boolean contentMatches(DSSDocument original, DSSDocument signed) {
        if (isXmlLike(original) && isXmlLike(signed)) {
            try {
                return canonicalXmlDigest(comparableXml(original)).equals(canonicalXmlDigest(comparableXml(signed)));
            } catch (Exception e) {
                return false;
            }
        }

        return Arrays.equals(readBytes(original), readBytes(signed));
    }

    private static Node comparableXml(DSSDocument document) {
        if (isXdcLike(document))
            return EFormUtils.getEformXmlFromXdcDocument(document);

        return EFormUtils.getXmlFromDocument(document);
    }

    private static String canonicalXmlDigest(Node node) {
        var canonicalized = XMLCanonicalizer.createInstance(CANONICALIZATION_METHOD).canonicalize(node);
        var digest = eu.europa.esig.dss.spi.DSSUtils.digest(DIGEST_ALGORITHM, canonicalized);
        return Base64.getEncoder().encodeToString(digest);
    }

    private static boolean isXmlLike(DSSDocument document) {
        var mimeType = document.getMimeType();
        if (mimeType != null && (isXML(mimeType) || isXDC(mimeType)))
            return true;

        return isXdcLike(document) || looksLikeXml(document);
    }

    private static boolean isXdcLike(DSSDocument document) {
        var mimeType = document.getMimeType();
        if (mimeType != null && isXDC(mimeType))
            return true;

        return XDCValidator.isXDCContent(document);
    }

    private static boolean looksLikeXml(DSSDocument document) {
        var bytes = readBytes(document);
        for (var b : bytes) {
            if (b == ' ' || b == '\t' || b == '\n' || b == '\r' || (b & 0xFF) == 0xEF || (b & 0xFF) == 0xBB
                    || (b & 0xFF) == 0xBF)
                continue;

            return b == '<';
        }

        return false;
    }

    private static byte[] readBytes(DSSDocument document) {
        try (var stream = document.openStream()) {
            return stream.readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static Set<String> signatureIdentities(List<AdvancedSignature> signatures) {
        var identities = new HashSet<String>();
        for (var signature : signatures)
            identities.add(identityOf(signature));

        return identities;
    }

    private static String identityOf(AdvancedSignature signature) {
        var certificateToken = signature.getSigningCertificateToken();
        if (certificateToken != null) {
            try {
                var certificate = Base64.getEncoder().encodeToString(certificateToken.getCertificate().getEncoded());
                var signingTime = signature.getSigningTime() != null ? signature.getSigningTime().getTime() : 0L;
                return "cert:" + certificate + "|time:" + signingTime;
            } catch (CertificateEncodingException e) {
                // fall through to id-based identity
            }
        }

        return "id:" + signature.getId();
    }

    private static DocumentRef toRef(DSSDocument document) {
        var mimeType = document.getMimeType() != null ? document.getMimeType().getMimeTypeString() : null;
        return new DocumentRef(document.getName(), mimeType);
    }

    private static SignatureRef toRef(AdvancedSignature signature) {
        String signedBy = null;
        var certificateToken = signature.getSigningCertificateToken();
        if (certificateToken != null)
            signedBy = certificateToken.getCertificate().getSubjectX500Principal().getName(X500Principal.RFC1779);

        var signingTime = signature.getSigningTime() != null ? DATE_FORMAT.format(signature.getSigningTime()) : null;
        return new SignatureRef(signedBy, signingTime);
    }
}
