package io.github.pigmesh.ai.deepseek.core;

public interface StreamingResponseHandling extends AsyncResponseHandling {

	StreamingCompletionHandling onComplete(Runnable streamingCompletionCallback);

}
