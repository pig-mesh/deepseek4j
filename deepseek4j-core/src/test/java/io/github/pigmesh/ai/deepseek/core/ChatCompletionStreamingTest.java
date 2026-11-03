package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The OpenAI-compatible streaming protocol terminates with a literal {@code data: [DONE]}
 * sentinel. That sentinel is a wire-level marker, not model output, so it must never be
 * handed to the caller as a chunk.
 */
class ChatCompletionStreamingTest {

	@Test
	void doneSentinelIsNotYieldedAsAStreamChunk() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(new MockResponse().setHeader("Content-Type", "text/event-stream")
					.setBody("data: {\"choices\":[{\"delta\":{\"content\":\"回答\"}}]}\n\ndata: [DONE]\n\n"));

			DeepSeekClient client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString())
					.openAiApiKey("test-key").build();
			try {
				List<ChatCompletionResponse> chunks = client.chatFluxCompletion("hello").collectList()
						.block(Duration.ofSeconds(10));

				assertThat(chunks).isNotNull();
				assertThat(chunks).hasSize(1);
				assertThat(chunks.get(0).choices().get(0).delta().content()).isEqualTo("回答");
			}
			finally {
				client.shutdown();
			}
		}
	}

	/**
	 * A stream that carries nothing but the sentinel must complete normally and yield no
	 * chunks, rather than throwing or hanging.
	 */
	@Test
	void emptyStreamYieldsNoChunksAndCompletesNormally() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(
					new MockResponse().setHeader("Content-Type", "text/event-stream").setBody("data: [DONE]\n\n"));

			DeepSeekClient client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString())
					.openAiApiKey("test-key").build();
			try {
				List<ChatCompletionResponse> chunks = client.chatFluxCompletion("hello").collectList()
						.block(Duration.ofSeconds(10));

				assertThat(chunks).isNotNull().isEmpty();
			}
			finally {
				client.shutdown();
			}
		}
	}

	/**
	 * Content that arrives after the sentinel must be dropped: once completion has been
	 * signalled, nothing more may reach the caller. This covers the case where OkHttp
	 * dispatches an event already buffered when the stream was cancelled.
	 */
	@Test
	void contentAfterTheSentinelIsDropped() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(new MockResponse().setHeader("Content-Type", "text/event-stream")
					.setBody("data: {\"choices\":[{\"delta\":{\"content\":\"回答\"}}]}\n\n" + "data: [DONE]\n\n"
							+ "data: {\"choices\":[{\"delta\":{\"content\":\"多余\"}}]}\n\n"));

			DeepSeekClient client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString())
					.openAiApiKey("test-key").build();
			try {
				List<ChatCompletionResponse> chunks = client.chatFluxCompletion("hello").collectList()
						.block(Duration.ofSeconds(10));

				assertThat(chunks).isNotNull().hasSize(1);
				assertThat(chunks.get(0).choices().get(0).delta().content()).isEqualTo("回答");
			}
			finally {
				client.shutdown();
			}
		}
	}

	/**
	 * The completion callback must fire exactly once, even though both the sentinel and a
	 * subsequent server close are able to complete the stream.
	 */
	@Test
	void completionCallbackRunsExactlyOnce() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(new MockResponse().setHeader("Content-Type", "text/event-stream")
					.setBody("data: {\"choices\":[{\"delta\":{\"content\":\"回答\"}}]}\n\ndata: [DONE]\n\n"));

			DeepSeekClient client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString())
					.openAiApiKey("test-key").build();
			try {
				AtomicInteger completions = new AtomicInteger();
				AtomicReference<Throwable> reported = new AtomicReference<>();
				CountDownLatch finished = new CountDownLatch(1);

				client.chatCompletion(new OpenAiClient.OpenAiClientContext(),
						ChatCompletionRequest.builder().addUserMessage("hello").build()).onPartialResponse(chunk -> {
						}).onComplete(() -> {
							completions.incrementAndGet();
							finished.countDown();
						}).onError(error -> {
							reported.set(error);
							finished.countDown();
						}).execute();

				assertThat(finished.await(10, TimeUnit.SECONDS)).isTrue();
				// give a stray second completion the chance to surface
				Thread.sleep(200);
				assertThat(completions).hasValue(1);
				// the cancellation that follows [DONE] must not surface as an error
				assertThat(reported).hasValue(null);
			}
			finally {
				client.shutdown();
			}
		}
	}

	/**
	 * Direct callbacks must not receive content after completion. The Flux path drops
	 * late content inside its sink, so only a direct-callback test can prove that the
	 * executor itself stops delivering.
	 */
	@Test
	void directCallbackReceivesNoContentAfterCompletion() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(new MockResponse().setHeader("Content-Type", "text/event-stream")
					.setBody("data: {\"choices\":[{\"delta\":{\"content\":\"回答\"}}]}\n\n" + "data: [DONE]\n\n"
							+ "data: {\"choices\":[{\"delta\":{\"content\":\"多余\"}}]}\n\n"));

			DeepSeekClient client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString())
					.openAiApiKey("test-key").build();
			try {
				List<String> received = new CopyOnWriteArrayList<>();
				AtomicInteger completions = new AtomicInteger();
				AtomicReference<Throwable> reported = new AtomicReference<>();
				CountDownLatch finished = new CountDownLatch(1);

				client.chatCompletion(new OpenAiClient.OpenAiClientContext(),
						ChatCompletionRequest.builder().addUserMessage("hello").build())
						.onPartialResponse(chunk -> received.add(chunk.choices().get(0).delta().content()))
						.onComplete(() -> {
							completions.incrementAndGet();
							finished.countDown();
						}).onError(error -> {
							reported.set(error);
							finished.countDown();
						}).execute();

				assertThat(finished.await(10, TimeUnit.SECONDS)).isTrue();
				// allow a late buffered event to reach the callback
				Thread.sleep(200);
				// the stream must end by completing, not by reporting an error
				assertThat(reported).hasValue(null);
				assertThat(completions).hasValue(1);
				assertThat(received).containsExactly("回答");
			}
			finally {
				client.shutdown();
			}
		}
	}

}
