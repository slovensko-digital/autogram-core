package digital.slovensko.autogram.core.errors;

public class InvalidSigningCertificateException extends AutogramException {
    public InvalidSigningCertificateException(Exception e) {
        super("Chyba podpisového certifikátu", "Podpisový certifikát nie je možné použiť v podpie", "Pri čítaní podpsiového certifikátu nastala chyba, ktorá bráni jeho použitiu v podpise.", e);
    }
}
