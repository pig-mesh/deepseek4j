package io.github.pigmesh.ai.deepseek.core;

import java.util.function.Consumer;

public interface AsyncResponseHandling {

	ErrorHandling onError(Consumer<Throwable> errorHandler);

	ErrorHandling ignoreErrors();

}
