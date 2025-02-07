package io.github.pigmesh.ai.deepseek.core;

import java.util.function.Consumer;

public interface SyncOrAsyncOrStreaming<ResponseContent> extends SyncOrAsync<ResponseContent> {

    StreamingResponseHandling onPartialResponse(Consumer<ResponseContent> partialResponseHandler);
}
