package io.github.pigmesh.ai.deepseek.core.chat;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.pigmesh.ai.deepseek.core.Json;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatCompletionRequestTest {

	@Test
	void serializesThinkingEnabled() throws Exception {
		ChatCompletionRequest request = ChatCompletionRequest.builder().model(ChatCompletionModel.DEEPSEEK_V4_PRO)
				.thinkingEnabled().addUserMessage("hello").build();

		JsonNode json = Json.OBJECT_MAPPER.readTree(Json.toJson(request));

		assertThat(json.path("model").asText()).isEqualTo("deepseek-v4-pro");
		assertThat(json.path("thinking").path("type").asText()).isEqualTo("enabled");
	}

	@Test
	void serializesThinkingDisabled() throws Exception {
		ChatCompletionRequest request = ChatCompletionRequest.builder().model(ChatCompletionModel.DEEPSEEK_V4_FLASH)
				.thinkingDisabled().addUserMessage("hello").build();

		JsonNode json = Json.OBJECT_MAPPER.readTree(Json.toJson(request));

		assertThat(json.path("model").asText()).isEqualTo("deepseek-v4-flash");
		assertThat(json.path("thinking").path("type").asText()).isEqualTo("disabled");
	}

	@Test
	void mapsCompatibleReasoningEffortValues() throws Exception {
		assertThat(reasoningEffortJson(ReasoningEffort.LOW)).isEqualTo("high");
		assertThat(reasoningEffortJson(ReasoningEffort.MEDIUM)).isEqualTo("high");
		assertThat(reasoningEffortJson(ReasoningEffort.HIGH)).isEqualTo("high");
		assertThat(reasoningEffortJson(ReasoningEffort.XHIGH)).isEqualTo("max");
		assertThat(reasoningEffortJson(ReasoningEffort.MAX)).isEqualTo("max");
	}

	@Test
	void mapsCompatibleReasoningEffortStrings() throws Exception {
		ChatCompletionRequest low = ChatCompletionRequest.builder().reasoningEffort("low").addUserMessage("hello")
				.build();
		ChatCompletionRequest xhigh = ChatCompletionRequest.builder().reasoningEffort("xhigh").addUserMessage("hello")
				.build();

		assertThat(Json.OBJECT_MAPPER.readTree(Json.toJson(low)).path("reasoning_effort").asText()).isEqualTo("high");
		assertThat(Json.OBJECT_MAPPER.readTree(Json.toJson(xhigh)).path("reasoning_effort").asText()).isEqualTo("max");
	}

	private String reasoningEffortJson(ReasoningEffort reasoningEffort) throws Exception {
		ChatCompletionRequest request = ChatCompletionRequest.builder().reasoningEffort(reasoningEffort)
				.addUserMessage("hello").build();
		return Json.OBJECT_MAPPER.readTree(Json.toJson(request)).path("reasoning_effort").asText();
	}

}
