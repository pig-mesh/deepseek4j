package io.github.pigmesh.ai.deepseek.core.handler;

public interface StreamingResponseHandling extends AsyncResponseHandling {

    StreamingCompletionHandling onComplete(Runnable streamingCompletionCallback);
}
