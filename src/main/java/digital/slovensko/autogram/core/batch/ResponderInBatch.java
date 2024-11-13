package digital.slovensko.autogram.core.batch;

import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;

public class ResponderInBatch implements Responder {
    private final Responder responder;
    private final Batch batch;

    public ResponderInBatch(Responder responder, Batch batch) {
        this.responder = responder;
        this.batch = batch;
    }

    public void onSuccess(SignedDocument signedDocument) {
        batch.onJobSuccess();
        responder.onSuccess(signedDocument);
    }

    public void onError(AutogramException error) {
        batch.onJobFailure();
        if (!error.batchCanContinue())
            batch.end();

        responder.onError(error);
    }
}
