package digital.slovensko.autogram.core.util;

import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.errors.UnsupportedSignatureLevelException;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.cades.validation.ASiCContainerWithCAdESValidatorFactory;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.asic.xades.validation.ASiCContainerWithXAdESValidatorFactory;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.cades.validation.CMSDocumentValidatorFactory;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidatorFactory;
import eu.europa.esig.dss.signature.AbstractSignatureService;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.xades.signature.XAdESService;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidatorFactory;
import sun.security.x509.X509CertImpl;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.cert.CertificateException;
import java.util.Base64;

public class DSSUtils {
    public static String parseCN(String rfc2253) {
        try {
            var ldapName = new LdapName(rfc2253);
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return rdn.getValue().toString();
                }
            }
        } catch (InvalidNameException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static CertificateToken parseCertificate(String certString) {
        try {
            return new CertificateToken(new X509CertImpl(Base64.getDecoder().decode(certString)));
        } catch (CertificateException e) {
            throw new UnrecognizedException(e); // TODO: custom exception
        }
    }

    public static String encodeCertificate(CertificateToken token) {
        return new String(Base64.getEncoder().encode(token.getEncoded()));
    }

    public static AbstractSignatureService getServiceForSignatureLevel(SignatureForm signatureForm, ASiCContainerType container, CertificateVerifier certificateVerifier) {
        return switch (signatureForm) {
            case XAdES -> container != null ? new ASiCWithXAdESService(certificateVerifier) : new XAdESService(certificateVerifier);
            case CAdES -> container != null ? new ASiCWithCAdESService(certificateVerifier) : new CAdESService(certificateVerifier);
            case PAdES -> new PAdESService(certificateVerifier);
            default -> throw new UnsupportedSignatureLevelException(signatureForm.name());
        };
    }

    public static String buildTooltipLabel(CertificateToken certificate) {
        var out = "";
        out += certificate.getSubject().getPrincipal().toString();
        if (certificate.getIssuer() != null) {
            out += "\n\n" + certificate.getIssuer().getPrincipal().toString();
        }
        return out;
    }

    public static SignedDocumentValidator createDocumentValidator(DSSDocument document) {
        if (new PDFDocumentValidatorFactory().isSupported(document))
            return new PDFDocumentValidatorFactory().create(document);

        if (new XMLDocumentValidatorFactory().isSupported(document))
            return new XMLDocumentValidatorFactory().create(document);

        if (new ASiCContainerWithXAdESValidatorFactory().isSupported(document))
            return new ASiCContainerWithXAdESValidatorFactory().create(document);

        if (new ASiCContainerWithCAdESValidatorFactory().isSupported(document))
            return new ASiCContainerWithCAdESValidatorFactory().create(document);

        if (new CMSDocumentValidatorFactory().isSupported(document))
            return new CMSDocumentValidatorFactory().create(document);

        return null;
    }

    public static String getXdcfFilename(String filename) {
        if (filename == null)
            return "document.xdcf";

        if (filename.endsWith(".xml"))
            return filename.replace(".xml", ".xdcf");

        if (!filename.contains(".xdcf"))
            return filename + ".xdcf";

        return filename;
    }

    public static String generatePrettyName(String newName, String originalName) {
        var lastDotIndex = originalName.lastIndexOf('.');
        var nameWithoutExtension = lastDotIndex == -1 ? originalName : originalName.substring(0, lastDotIndex);
        var extension = generatePrettyExtension(newName.substring(newName.lastIndexOf('.') + 1));

        return nameWithoutExtension + "_signed." + extension;
    }

    public static String generatePrettyExtension(String extension) {
        return switch (extension) {
            case "scs" -> "asics";
            case "sce" -> "asice";
            default -> extension;
        };
    }
}
