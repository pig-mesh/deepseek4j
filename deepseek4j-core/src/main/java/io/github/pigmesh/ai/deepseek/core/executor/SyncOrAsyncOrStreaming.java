package io.github.pigmesh.ai.deepseek.core.executor;

import io.github.pigmesh.ai.deepseek.core.handler.StreamingResponseHandling;

import java.util.function.Consumer;

public interface SyncOrAsyncOrStreaming<ResponseContent> extends SyncOrAsync<ResponseContent> {

    StreamingResponseHandling onPartialResponse(Consumer<ResponseContent> partialResponseHandler);
}
