package io.github.pigmesh.ai.deepseek.core;

/**
 * Provides a mechanism to cancel the response after a request has been initiated.
 */
public class ResponseHandle {

	volatile boolean cancelled = false;

	/**
	 * Cancels the response. Currently, this only works for streaming. It has no effect on
	 * regular asynchronous responses.
	 */
	public void cancel() {
		cancelled = true;
	}

}
