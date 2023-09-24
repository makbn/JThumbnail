package io.github.makbn.jthumbnail.core.model;

import lombok.Data;

@Data
public class ExecutionResult {
    private final boolean generated;
    private Throwable exception;

    public ExecutionResult(boolean generated) {
        this.generated = generated;
    }

    public ExecutionResult(boolean generated, Throwable exception) {
        this.generated = generated;
        this.exception = exception;
    }

    public static ExecutionResult success() {
        return new ExecutionResult(true);
    }

    public static ExecutionResult failed(Throwable e) {
        return new ExecutionResult(false, e);
    }

    public boolean hasException() {
        return exception != null;
    }
}
