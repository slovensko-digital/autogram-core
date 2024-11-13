package digital.slovensko.autogram.core.dto;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

public record SigningKey (AbstractKeyStoreTokenConnection token, DSSPrivateKeyEntry privateKey) {
    public SignatureValue sign(ToBeSigned dataToSign, DigestAlgorithm algo) {
        return token.sign(dataToSign, algo, privateKey);
    }

    public CertificateToken getCertificate() {
        return privateKey.getCertificate();
    }

    public CertificateToken[] getCertificateChain() {
        return privateKey.getCertificateChain();
    }

    public void close() {
        token.close();
    }
}
