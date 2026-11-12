package io.github.pigmesh.ai.deepseek.core.chat;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.pigmesh.ai.deepseek.core.Json;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
		ChatCompletionRequest request = ChatCompletionRequest.builder().model(ChatCompletionModel.DEEPSEEK_FLASH)
				.thinkingDisabled().addUserMessage("hello").build();

		JsonNode json = Json.OBJECT_MAPPER.readTree(Json.toJson(request));

		assertThat(json.path("model").asText()).isEqualTo("deepseek-flash");
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

	@Test
	void serializesExtraEntriesAtTheTopLevel() throws Exception {
		ChatCompletionRequest request = ChatCompletionRequest.builder().model(ChatCompletionModel.DEEPSEEK_V4_PRO)
				.addUserMessage("hello").extra("enable_thinking", false).extra("top_k", 20).build();

		JsonNode json = Json.OBJECT_MAPPER.readTree(Json.toJson(request));

		assertThat(json.path("model").asText()).isEqualTo("deepseek-v4-pro");
		assertThat(json.has("extra_body")).isFalse();
		assertThat(json.path("enable_thinking").isBoolean()).isTrue();
		assertThat(json.path("enable_thinking").asBoolean()).isFalse();
		assertThat(json.path("top_k").asInt()).isEqualTo(20);
	}

	@Test
	void copiesExtraBodyFromTheGivenMap() throws Exception {
		Map<String, Object> extra = new HashMap<>();
		extra.put("enable_thinking", false);

		ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("hello").extraBody(extra)
				.build();
		extra.put("top_k", 20);

		JsonNode json = Json.OBJECT_MAPPER.readTree(Json.toJson(request));

		assertThat(json.path("enable_thinking").asBoolean()).isFalse();
		assertThat(json.has("top_k")).isFalse();
	}

	@Test
	void collectsUnknownPropertiesIntoExtraBodyWhenDeserializing() throws Exception {
		ChatCompletionRequest request = Json.OBJECT_MAPPER.readValue(
				"{\"model\":\"deepseek-chat\",\"messages\":[],\"enable_thinking\":false}", ChatCompletionRequest.class);

		assertThat(request.extraBody()).containsEntry("enable_thinking", false);
	}

	@Test
	void builderMutationAfterBuildDoesNotAffectBuiltRequest() {
		ChatCompletionRequest.Builder builder = ChatCompletionRequest.builder().addUserMessage("hello").extra("top_k",
				20);
		ChatCompletionRequest first = builder.build();
		builder.extra("enable_thinking", false);
		ChatCompletionRequest second = builder.build();

		assertThat(first.extraBody()).containsOnlyKeys("top_k");
		assertThat(second.extraBody()).containsOnlyKeys("top_k", "enable_thinking");
	}

	@Test
	void deserializingExplicitExtraBodyNullKeepsOtherExtras() throws Exception {
		ChatCompletionRequest request = Json.OBJECT_MAPPER
				.readValue("{\"messages\":[],\"top_k\":20,\"extra_body\":null}", ChatCompletionRequest.class);

		assertThat(request.extraBody()).containsEntry("top_k", 20);
	}

	@Test
	void deserializingLiteralExtraBodyKeepsItAsExtraProperty() throws Exception {
		ChatCompletionRequest request = Json.OBJECT_MAPPER.readValue("{\"messages\":[],\"extra_body\":{\"top_k\":20}}",
				ChatCompletionRequest.class);

		assertThat(request.extraBody()).containsEntry("extra_body", Collections.singletonMap("top_k", 20));

		JsonNode json = Json.OBJECT_MAPPER.readTree(Json.toJson(request));

		assertThat(json.path("extra_body").path("top_k").asInt()).isEqualTo(20);
	}

	@Test
	void deserializingExtraBodyDoesNotDropOtherExtras() throws Exception {
		ChatCompletionRequest request = Json.OBJECT_MAPPER.readValue(
				"{\"messages\":[],\"top_k\":20,\"extra_body\":{\"enable_thinking\":false}}",
				ChatCompletionRequest.class);

		assertThat(request.extraBody()).containsEntry("top_k", 20).containsEntry("extra_body",
				Collections.singletonMap("enable_thinking", false));
	}

	@Test
	void copyingRequestWithoutExtrasClearsBuilderExtras() {
		ChatCompletionRequest source = ChatCompletionRequest.builder().addUserMessage("hello").build();
		ChatCompletionRequest request = ChatCompletionRequest.builder().extra("top_k", 20).from(source).build();

		assertThat(request.extraBody()).isNull();
	}

	private String reasoningEffortJson(ReasoningEffort reasoningEffort) throws Exception {
		ChatCompletionRequest request = ChatCompletionRequest.builder().reasoningEffort(reasoningEffort)
				.addUserMessage("hello").build();
		return Json.OBJECT_MAPPER.readTree(Json.toJson(request)).path("reasoning_effort").asText();
	}

}
