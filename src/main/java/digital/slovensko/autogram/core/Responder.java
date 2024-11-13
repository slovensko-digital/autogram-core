package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;

public interface Responder {
    void onSuccess(SignedDocument signedDocument);

    void onError(AutogramException error);
}
