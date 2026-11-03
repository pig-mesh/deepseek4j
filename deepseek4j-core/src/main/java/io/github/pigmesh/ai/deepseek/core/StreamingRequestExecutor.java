package io.github.pigmesh.ai.deepseek.core;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.pigmesh.ai.deepseek.core.Utils.toException;

class StreamingRequestExecutor<Request, Response, ResponseContent> {

	private static final Logger log = LoggerFactory.getLogger(StreamingRequestExecutor.class);

	private final OkHttpClient okHttpClient;

	private final String endpointUrl;

	private final Supplier<Request> requestWithStreamSupplier;

	private final Class<Response> responseClass;

	private final Function<Response, ResponseContent> streamEventContentExtractor;

	private final boolean logStreamingResponses;

	private final ResponseLoggingInterceptor responseLogger = new ResponseLoggingInterceptor();

	StreamingRequestExecutor(OkHttpClient okHttpClient, String endpointUrl, Supplier<Request> requestWithStreamSupplier,
			Class<Response> responseClass, Function<Response, ResponseContent> streamEventContentExtractor,
			boolean logStreamingResponses) {
		this.okHttpClient = okHttpClient;
		this.endpointUrl = endpointUrl;
		this.requestWithStreamSupplier = requestWithStreamSupplier;
		this.responseClass = responseClass;
		this.streamEventContentExtractor = streamEventContentExtractor;
		this.logStreamingResponses = logStreamingResponses;
	}

	StreamingResponseHandling onPartialResponse(Consumer<ResponseContent> partialResponseHandler) {

		return new StreamingResponseHandling() {

			@Override
			public StreamingCompletionHandling onComplete(Runnable streamingCompletionCallback) {
				return new StreamingCompletionHandling() {

					@Override
					public ErrorHandling onError(Consumer<Throwable> errorHandler) {
						return new ErrorHandling() {

							@Override
							public ResponseHandle execute() {
								return stream(partialResponseHandler, streamingCompletionCallback, errorHandler);
							}
						};
					}

					@Override
					public ErrorHandling ignoreErrors() {
						return new ErrorHandling() {

							@Override
							public ResponseHandle execute() {
								return stream(partialResponseHandler, streamingCompletionCallback, (e) -> {
									// intentionally ignoring because user called
									// ignoreErrors()
								});
							}
						};
					}
				};
			}

			@Override
			public ErrorHandling onError(Consumer<Throwable> errorHandler) {
				return new ErrorHandling() {

					@Override
					public ResponseHandle execute() {
						return stream(partialResponseHandler, () -> {
							// intentionally ignoring because user did not provide
							// callback
						}, errorHandler);
					}
				};
			}

			@Override
			public ErrorHandling ignoreErrors() {
				return new ErrorHandling() {

					@Override
					public ResponseHandle execute() {
						return stream(partialResponseHandler, () -> {
							// intentionally ignoring because user did not provide
							// callback
						}, (e) -> {
							// intentionally ignoring because user called ignoreErrors()
						});
					}
				};
			}
		};
	}

	private ResponseHandle stream(Consumer<ResponseContent> partialResponseHandler,
			Runnable streamingCompletionCallback, Consumer<Throwable> errorHandler) {

		Request request = requestWithStreamSupplier.get();

		String requestJson = Json.toJson(request);

		okhttp3.Request okHttpRequest = new okhttp3.Request.Builder().url(endpointUrl)
				.post(RequestBody.create(requestJson, MediaType.get("application/json; charset=utf-8"))).build();

		ResponseHandle responseHandle = new ResponseHandle();

		// Completion must be signalled exactly once, whether the stream ends with
		// the [DONE] sentinel, a normal server close, or an I/O error.
		AtomicBoolean completed = new AtomicBoolean(false);
		Runnable completeOnce = () -> {
			if (completed.compareAndSet(false, true)) {
				streamingCompletionCallback.run();
			}
		};

		EventSourceListener eventSourceListener = new EventSourceListener() {

			@Override
			public void onOpen(EventSource eventSource, okhttp3.Response response) {
				if (responseHandle.cancelled) {
					eventSource.cancel();
					return;
				}

				if (logStreamingResponses) {
					responseLogger.log(response);
				}
			}

			@Override
			public void onEvent(EventSource eventSource, String id, String type, String data) {
				if (responseHandle.cancelled) {
					eventSource.cancel();
					return;
				}

				// OkHttp may still dispatch an event it had already buffered when
				// [DONE] completed the stream; never hand content to the caller once
				// completion has been signalled. Covered by
				// ChatCompletionStreamingTest#directCallbackReceivesNoContentAfterCompletion,
				// which fails if this guard is removed.
				if (completed.get()) {
					return;
				}

				if (logStreamingResponses) {
					log.debug("onEvent() {}", data);
				}

				try {
					// The OpenAI-compatible protocol ends the stream with a literal
					// "[DONE]" sentinel. It is a transport marker, not model output:
					// yielding it as content surfaces "[DONE]" to the caller and
					// corrupts multi-turn conversations that echo streamed text back.
					if ("[DONE]".equals(data)) {
						// Signal completion before cancelling, so that any onFailure
						// triggered by the cancel sees completed == true and is
						// silently dropped instead of surfacing as an error. The
						// cancel sits in a finally so that a throwing completion
						// callback cannot leave the SSE reader running.
						try {
							completeOnce.run();
						}
						finally {
							eventSource.cancel();
						}
						return;
					}

					Response response = Json.fromJson(data, responseClass);
					ResponseContent responseContent = streamEventContentExtractor.apply(response);
					if (responseContent != null) {
						partialResponseHandler.accept(responseContent); // do not handle
																		// exception,
																		// fail-fast
					}
				}
				catch (Exception e) {
					errorHandler.accept(e);
				}
			}

			@Override
			public void onClosed(EventSource eventSource) {
				if (responseHandle.cancelled) {
					eventSource.cancel();
					return;
				}

				if (logStreamingResponses) {
					log.debug("onClosed()");
				}

				completeOnce.run();
			}

			@Override
			public void onFailure(EventSource eventSource, Throwable t, okhttp3.Response response) {
				if (responseHandle.cancelled) {
					return;
				}

				// The stream already completed through the [DONE] sentinel; the
				// cancellation that followed it must not surface as an error.
				// Note: a genuine I/O error racing with [DONE] would also be
				// swallowed here. Receiving [DONE] means the stream was healthy, so
				// that is accepted for now - revisit if an error-reporting layer is
				// added above this class.
				if (completed.get()) {
					return;
				}

				// TODO remove this when migrating from okhttp
				if (t instanceof IllegalArgumentException && "byteCount < 0: -1".equals(t.getMessage())) {
					completeOnce.run();
					return;
				}

				if (logStreamingResponses) {
					log.debug("onFailure()", t);
					responseLogger.log(response);
				}

				if (t != null) {
					errorHandler.accept(t); // TODO also include information from
											// response?
				}
				else {
					try {
						errorHandler.accept(toException(response));
					}
					catch (IOException e) {
						errorHandler.accept(e); // TODO right thing to do?
					}
				}
			}
		};

		EventSource eventSource = EventSources.createFactory(okHttpClient).newEventSource(okHttpRequest,
				eventSourceListener);
		responseHandle.onCancel(eventSource::cancel);

		return responseHandle;
	}

}
