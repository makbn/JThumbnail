package io.github.makbn.jthumbnail.core.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Data
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequiredArgsConstructor
public class ExecutionResult {
    boolean generated;
    @NonFinal
    Throwable exception;

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
