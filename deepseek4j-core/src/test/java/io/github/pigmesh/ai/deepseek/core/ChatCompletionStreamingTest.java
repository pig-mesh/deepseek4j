package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

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

}
