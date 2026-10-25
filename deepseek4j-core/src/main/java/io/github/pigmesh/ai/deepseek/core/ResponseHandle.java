package io.github.pigmesh.ai.deepseek.core;

/**
 * Provides a mechanism to cancel the response after a request has been initiated.
 */
public class ResponseHandle {

	volatile boolean cancelled = false;

	private Runnable cancellation;

	synchronized void onCancel(Runnable cancellation) {
		if (cancelled) {
			cancellation.run();
		}
		else {
			this.cancellation = cancellation;
		}
	}

	/**
	 * Cancels the response. Currently, this only works for streaming. It has no effect on
	 * regular asynchronous responses.
	 */
	public synchronized void cancel() {
		if (cancelled) {
			return;
		}
		cancelled = true;
		if (cancellation != null) {
			cancellation.run();
			cancellation = null;
		}
	}

}
