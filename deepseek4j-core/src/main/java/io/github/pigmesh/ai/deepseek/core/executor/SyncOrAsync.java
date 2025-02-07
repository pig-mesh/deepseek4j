package io.github.pigmesh.ai.deepseek.core.executor;

import io.github.pigmesh.ai.deepseek.core.handler.AsyncResponseHandling;

import java.util.function.Consumer;

public interface SyncOrAsync<ResponseContent> {

    ResponseContent execute();

    AsyncResponseHandling onResponse(Consumer<ResponseContent> responseHandler);
}
