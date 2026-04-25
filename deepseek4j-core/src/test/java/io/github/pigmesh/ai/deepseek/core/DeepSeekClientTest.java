package io.github.pigmesh.ai.deepseek.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ReasoningEffort;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekClientTest {

	@Test
	void configUsesDeepSeekV4ProAndThinkingByDefault() {
		DeepSeekConfig config = new DeepSeekConfig();

		assertThat(config.getModel()).isEqualTo("deepseek-v4-pro");
		assertThat(config.isThinkingEnabled()).isTrue();
		assertThat(config.getReasoningEffort()).isEqualTo("high");
	}

	@Test
	void appliesDefaultThinkingOptionsWithoutMutatingOriginalRequest() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
					.setBody("{\"id\":\"chatcmpl-test\",\"created\":1,\"model\":\"deepseek-v4-pro\",\"choices\":[]}"));
			DeepSeekClient client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString())
					.openAiApiKey("test-key").thinkingEnabled(true).reasoningEffort(ReasoningEffort.HIGH).build();
			try {
				ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("hello").build();

				client.chatCompletion(request).execute();

				RecordedRequest recordedRequest = server.takeRequest();
				JsonNode json = Json.OBJECT_MAPPER.readTree(recordedRequest.getBody().readUtf8());
				assertThat(json.path("model").asText()).isEqualTo("deepseek-v4-pro");
				assertThat(json.path("thinking").path("type").asText()).isEqualTo("enabled");
				assertThat(json.path("reasoning_effort").asText()).isEqualTo("high");
				assertThat(request.model()).isNull();
				assertThat(request.thinking()).isNull();
				assertThat(request.reasoningEffort()).isNull();
			}
			finally {
				client.shutdown();
			}
		}
	}

	@Test
	void requestLevelThinkingOptionsOverrideClientDefaults() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody(
					"{\"id\":\"chatcmpl-test\",\"created\":1,\"model\":\"deepseek-v4-flash\",\"choices\":[]}"));
			DeepSeekClient client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString())
					.openAiApiKey("test-key").model("deepseek-v4-pro").thinkingEnabled(true)
					.reasoningEffort(ReasoningEffort.HIGH).build();
			try {
				ChatCompletionRequest request = ChatCompletionRequest.builder().model("deepseek-v4-flash")
						.thinkingDisabled().reasoningEffort(ReasoningEffort.MAX).addUserMessage("hello").build();

				client.chatCompletion(request).execute();

				RecordedRequest recordedRequest = server.takeRequest();
				JsonNode json = Json.OBJECT_MAPPER.readTree(recordedRequest.getBody().readUtf8());
				assertThat(json.path("model").asText()).isEqualTo("deepseek-v4-flash");
				assertThat(json.path("thinking").path("type").asText()).isEqualTo("disabled");
				assertThat(json.path("reasoning_effort").asText()).isEqualTo("max");
			}
			finally {
				client.shutdown();
			}
		}
	}

}
