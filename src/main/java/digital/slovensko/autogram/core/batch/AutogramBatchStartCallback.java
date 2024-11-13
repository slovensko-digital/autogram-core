package digital.slovensko.autogram.core.batch;

import digital.slovensko.autogram.core.dto.SigningKey;
import digital.slovensko.autogram.core.errors.AutogramException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class AutogramBatchStartCallback implements Consumer<SigningKey> {

    private final Batch batch;
    private final BatchResponder responder;
    private static final Logger LOGGER = LoggerFactory.getLogger(AutogramBatchStartCallback.class);

    public AutogramBatchStartCallback(Batch batch, BatchResponder responder) {
        this.batch = batch;
        this.responder = responder;
    }

    public void accept(SigningKey key) {
        try {
            LOGGER.info("Starting batch");
            batch.start(key);
            handleSuccess();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        if (e instanceof AutogramException)
            responder.onBatchStartFailure((AutogramException) e);
        else {
            LOGGER.info("Batch start failed with exception: " + e);
            responder.onBatchStartFailure(
                    new AutogramException("Unkown error occured while starting batch", "",
                            "Batch start failed with exception: " + e, e));
        }
    }

    private void handleSuccess() {
        responder.onBatchStartSuccess(batch);
    }
};
