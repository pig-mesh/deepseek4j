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

}
