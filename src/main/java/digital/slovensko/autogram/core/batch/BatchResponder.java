package digital.slovensko.autogram.core.batch;

import digital.slovensko.autogram.core.errors.AutogramException;

public interface BatchResponder {
    abstract public void onBatchStartSuccess(Batch batch);

    abstract public void onBatchStartFailure(AutogramException error);

    abstract public void onBatchSignFailed(AutogramException error);
}
