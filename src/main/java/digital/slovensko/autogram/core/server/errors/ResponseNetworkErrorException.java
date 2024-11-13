package digital.slovensko.autogram.core.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class ResponseNetworkErrorException extends AutogramException {
    public ResponseNetworkErrorException(String message, Exception e) {
        super("Nastala chyba", "Nepodarilo sa poslať odpoveď externej aplikácii", message, e);
    }
}
