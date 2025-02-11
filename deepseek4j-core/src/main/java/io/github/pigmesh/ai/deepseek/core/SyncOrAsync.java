package io.github.pigmesh.ai.deepseek.core;

import java.util.function.Consumer;

public interface SyncOrAsync<ResponseContent> {

	ResponseContent execute();

	AsyncResponseHandling onResponse(Consumer<ResponseContent> responseHandler);

}
