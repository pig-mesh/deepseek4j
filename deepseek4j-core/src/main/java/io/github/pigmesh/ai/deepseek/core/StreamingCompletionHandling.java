package io.github.pigmesh.ai.deepseek.core;

import java.util.function.Consumer;

public interface StreamingCompletionHandling {

    ErrorHandling onError(Consumer<Throwable> errorHandler);

    ErrorHandling ignoreErrors();
}
