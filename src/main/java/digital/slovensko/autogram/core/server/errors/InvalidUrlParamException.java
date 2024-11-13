package digital.slovensko.autogram.core.server.errors;

import digital.slovensko.autogram.core.errors.AutogramException;

public class InvalidUrlParamException extends AutogramException {
    public InvalidUrlParamException(String description) {
        super("Invalid url parameter", "Something is wrong with url parameter", description);
    }
}
