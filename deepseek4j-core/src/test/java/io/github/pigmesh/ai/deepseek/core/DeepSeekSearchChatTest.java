package io.github.pigmesh.ai.deepseek.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.chat.ReasoningEffort;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchRequest;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchResponse;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchResult;
import okhttp3.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepSeekSearchChatTest {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private MockWebServer server;

	private DeepSeekClient client;

	private Dispatcher dispatcher;

	@BeforeEach
	void setUp() {
		server = new MockWebServer();
		dispatcher = new Dispatcher();
		client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString()).openAiApiKey("test-key")
				.dispatcher(dispatcher).model("deepseek-v4-pro").systemMessage("客户端系统提示").thinkingEnabled(true)
				.reasoningEffort(ReasoningEffort.MAX).customHeaders(Collections.singletonMap("X-Client", "shared"))
				.build();
	}

	@AfterEach
	void tearDown() throws Exception {
		dispatcher.cancelAll();
		client.shutdown();
		server.close();
	}

	@Test
	void searchesThenStreamsUsingClientDefaultsAndSharedHttpSettings() throws Exception {
		enqueueResults("摘要", "正文");
		enqueueChat();

		List<ChatCompletionResponse> responses = client.chatSearchCompletion("问题").collectList().block(TIMEOUT);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).choices().get(0).delta().content()).isEqualTo("回答");
		RecordedRequest search = takeRequest("/v1/tools/search");
		assertThat(body(search))
				.isEqualTo(Json.OBJECT_MAPPER.readTree("{\"query\":\"问题\",\"count\":5,\"need_content\":false}"));
		JsonNode chat = body(takeRequest("/v1/chat/completions"));
		assertThat(chat.path("model").asText()).isEqualTo("deepseek-v4-pro");
		assertThat(chat.path("thinking").path("type").asText()).isEqualTo("enabled");
		assertThat(chat.path("reasoning_effort").asText()).isEqualTo("max");
		assertThat(chat.path("stream").asBoolean()).isTrue();
		assertThat(chat.path("messages")).hasSize(3);
		assertThat(chat.path("messages").get(0).path("content").asText()).isEqualTo("客户端系统提示");
		assertThat(chat.path("messages").get(1).path("role").asText()).isEqualTo("user");
		assertThat(chat.path("messages").get(1).path("content").asText())
				.contains("来源标题", "https://example.com/source", "摘要").doesNotContain("正文");
		assertThat(chat.path("messages").get(2).path("content").asText()).isEqualTo("问题");
	}

	@Test
	void preservesConversationAndExplicitOptionsWithoutMutatingTheRequest() throws Exception {
		enqueueResults("摘要", "正文");
		enqueueChat();
		ChatCompletionRequest request = ChatCompletionRequest.builder().model("custom-model").thinkingDisabled()
				.reasoningEffort(ReasoningEffort.HIGH).temperature(0.4).maxTokens(512).addSystemMessage("请求系统提示")
				.addUserMessage("历史问题").addAssistantMessage("历史回答").addUserMessage("当前问题").build();
		String original = Json.toJson(request);
		DeepSeekSearchRequest search = DeepSeekSearchRequest.builder().query("短查询").count(2).needContent(false)
				.localization("1.2.3.4").oriQuestion("原问题").build();

		client.chatSearchCompletion(request, search).blockLast(TIMEOUT);

		assertThat(body(takeRequest("/v1/tools/search"))).isEqualTo(Json.OBJECT_MAPPER.readTree(Json.toJson(search)));
		ObjectNode chat = (ObjectNode) body(takeRequest("/v1/chat/completions"));
		assertThat(chat.path("messages")).hasSize(5);
		assertThat(chat.path("messages").get(3).path("content").asText()).contains("摘要").doesNotContain("正文");
		((com.fasterxml.jackson.databind.node.ArrayNode) chat.path("messages")).remove(3);
		chat.remove("stream");
		assertThat(chat).isEqualTo(Json.OBJECT_MAPPER.readTree(original));
		assertThat(Json.toJson(request)).isEqualTo(original);
	}

	@Test
	void addsConfiguredSystemMessageToFullRequestAndKeepsLongQuestion() throws Exception {
		enqueueResults("摘要", "正文");
		enqueueChat();
		String question = String.join("", Collections.nCopies(200, "详细问题"));
		ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("历史问题")
				.addAssistantMessage("历史回答").addUserMessage(question).build();

		client.chatSearchCompletion(request, DeepSeekSearchRequest.builder().query("短查询").needContent(true).build())
				.blockLast(TIMEOUT);

		takeRequest("/v1/tools/search");
		JsonNode messages = body(takeRequest("/v1/chat/completions")).path("messages");
		assertThat(messages).hasSize(5);
		assertThat(messages.get(0).path("content").asText()).isEqualTo("客户端系统提示");
		assertThat(messages.get(3).path("content").asText()).contains("正文").doesNotContain("摘要");
		assertThat(messages.get(4).path("content").asText()).isEqualTo(question);
		assertThat(request.messages()).hasSize(3);
	}

	@Test
	void stringOverloadKeepsLongQuestionAndUsesSnippetWhenSummaryIsMissing() throws Exception {
		enqueueResults("", "");
		enqueueChat();
		String question = String.join("", Collections.nCopies(200, "长问题"));

		client.chatSearchCompletion(question, DeepSeekSearchRequest.builder().query("短查询").needContent(true).build())
				.blockLast(TIMEOUT);

		takeRequest("/v1/tools/search");
		JsonNode messages = body(takeRequest("/v1/chat/completions")).path("messages");
		assertThat(messages.get(1).path("content").asText()).contains("简短片段");
		assertThat(messages.get(2).path("content").asText()).isEqualTo(question);
	}

	@Test
	void emptyResultsContinueWithOriginalConversation() throws Exception {
		server.enqueue(json("{\"results\":[]}"));
		enqueueChat();
		client.chatSearchCompletion("没有结果的问题").blockLast(TIMEOUT);
		takeRequest("/v1/tools/search");
		JsonNode messages = body(takeRequest("/v1/chat/completions")).path("messages");
		assertThat(messages).hasSize(2);
		assertThat(messages.get(1).path("content").asText()).isEqualTo("没有结果的问题");
	}

	@Test
	void eachSubscriptionStartsAnIndependentSearchWithoutEagerIo() throws Exception {
		Flux<ChatCompletionResponse> responses = client.chatSearchCompletion("问题");
		assertThat(server.getRequestCount()).isZero();
		for (int i = 0; i < 2; i++) {
			server.enqueue(json("{\"results\":[]}"));
			enqueueChat();
			responses.blockLast(TIMEOUT);
			takeRequest("/v1/tools/search");
			takeRequest("/v1/chat/completions");
		}
	}

	@Test
	void invalidQueryFailsBeforeAnyNetworkRequestAndCountsUnicodeCodePoints() throws Exception {
		String emoji = "🔎";
		assertThatThrownBy(
				() -> client.chatSearchCompletion(String.join("", Collections.nCopies(129, emoji))).blockLast(TIMEOUT))
						.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("shorter query");
		assertThatThrownBy(() -> client.chatSearchCompletion(" \u3000").blockLast(TIMEOUT))
				.isInstanceOf(IllegalArgumentException.class);
		assertThat(server.getRequestCount()).isZero();
		server.enqueue(json("{\"results\":[]}"));
		enqueueChat();
		String query = String.join("", Collections.nCopies(128, emoji));
		client.chatSearchCompletion(query).blockLast(TIMEOUT);
		assertThat(body(takeRequest("/v1/tools/search")).path("query").asText()).isEqualTo(query);
		takeRequest("/v1/chat/completions");
	}

	@Test
	void boundsLargeSearchContextWithoutTruncatingTheQuestion() throws Exception {
		String content = String.join("", Collections.nCopies(10000, "🔎"));
		DeepSeekSearchResult result = DeepSeekSearchResult.builder().title("来源").url("https://example.com")
				.content(content).build();
		server.enqueue(
				json(Json.toJson(DeepSeekSearchResponse.builder().results(Collections.nCopies(50, result)).build())));
		enqueueChat();
		client.chatSearchCompletion("问题",
				DeepSeekSearchRequest.builder().query("问题").count(50).needContent(true).build()).blockLast(TIMEOUT);
		takeRequest("/v1/tools/search");
		JsonNode messages = body(takeRequest("/v1/chat/completions")).path("messages");
		String context = messages.get(1).path("content").asText();
		assertThat(context.length()).isLessThanOrEqualTo(20000);
		assertThat(context).contains("🔎", "…").doesNotContain("�");
		assertThat(messages.get(2).path("content").asText()).isEqualTo("问题");
	}

	@ParameterizedTest
	@ValueSource(ints = { 400, 401, 429, 500 })
	void searchHttpErrorsStopBeforeCallingTheModel(int status) throws Exception {
		server.enqueue(json("{\"error\":{\"message\":\"search failed\"}}").setResponseCode(status));
		assertThatThrownBy(() -> client.chatSearchCompletion("问题").blockLast(TIMEOUT))
				.isInstanceOfSatisfying(OpenAiHttpException.class, e -> assertThat(e.code()).isEqualTo(status));
		takeRequest("/v1/tools/search");
		assertThat(server.getRequestCount()).isEqualTo(1);
	}

	@Test
	void configuredCallTimeoutReachesTheSubscriber() {
		client.shutdown();
		client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString()).openAiApiKey("test-key")
				.callTimeout(Duration.ofMillis(200)).readTimeout(Duration.ofSeconds(5)).build();
		server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
		CompletableFuture<List<ChatCompletionResponse>> response = client.chatSearchCompletion("问题").collectList()
				.toFuture();
		assertThatThrownBy(() -> response.get(2, TimeUnit.SECONDS))
				.isInstanceOf(java.util.concurrent.ExecutionException.class).cause()
				.isInstanceOf(java.io.InterruptedIOException.class).hasMessageContaining("timeout");
		assertThat(server.getRequestCount()).isEqualTo(1);
	}

	@Test
	void searchTransportErrorsStopBeforeCallingTheModel() {
		server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
		assertThatThrownBy(() -> client.chatSearchCompletion("问题").blockLast(TIMEOUT))
				.hasRootCauseInstanceOf(java.io.IOException.class);
		assertThat(server.getRequestCount()).isEqualTo(1);
	}

	@Test
	void invalidSearchJsonStopsBeforeCallingTheModel() {
		server.enqueue(json("not json"));
		assertThatThrownBy(() -> client.chatSearchCompletion("问题").blockLast(TIMEOUT))
				.hasCauseInstanceOf(com.fasterxml.jackson.core.JsonParseException.class);
		assertThat(server.getRequestCount()).isEqualTo(1);
	}

	@Test
	void missingSearchResponseIsAnErrorRatherThanAnEmptyAnswer() {
		server.enqueue(new MockResponse().setResponseCode(204));
		assertThatThrownBy(() -> client.chatSearchCompletion("问题").blockLast(TIMEOUT))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("empty response body");
		assertThat(server.getRequestCount()).isEqualTo(1);
	}

	@Test
	void modelErrorsReachTheSubscriber() throws Exception {
		server.enqueue(json("{\"results\":[]}"));
		server.enqueue(json("{\"error\":{\"message\":\"model failed\"}}").setResponseCode(503));
		assertThatThrownBy(() -> client.chatSearchCompletion("问题").blockLast(TIMEOUT))
				.isInstanceOfSatisfying(OpenAiHttpException.class, e -> assertThat(e.code()).isEqualTo(503));
		assertThat(server.getRequestCount()).isEqualTo(2);
	}

	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void cancellationStopsTheActiveHttpRequest(boolean duringChat) throws Exception {
		if (duringChat) {
			server.enqueue(json("{\"results\":[]}"));
		}
		server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
		CompletableFuture<Throwable> error = new CompletableFuture<>();
		Disposable subscription = client.chatSearchCompletion("问题").subscribe(ignored -> {
		}, error::complete);
		takeRequest("/v1/tools/search");
		if (duringChat) {
			takeRequest("/v1/chat/completions");
		}
		CountDownLatch idle = new CountDownLatch(1);
		dispatcher.setIdleCallback(idle::countDown);

		subscription.dispose();

		assertThat(idle.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(dispatcher.runningCallsCount()).isZero();
		assertThat(error.isDone()).isFalse();
		assertThat(server.getRequestCount()).isEqualTo(duringChat ? 2 : 1);
	}

	private void enqueueResults(String summary, String content) {
		DeepSeekSearchResult result = DeepSeekSearchResult.builder().title("来源标题").url("https://example.com/source")
				.snippet("简短片段").summary(summary).content(content).build();
		server.enqueue(
				json(Json.toJson(DeepSeekSearchResponse.builder().results(Collections.singletonList(result)).build())));
	}

	private void enqueueChat() {
		server.enqueue(new MockResponse().setHeader("Content-Type", "text/event-stream")
				.setBody("data: {\"choices\":[{\"delta\":{\"content\":\"回答\"}}]}\n\ndata: [DONE]\n\n"));
	}

	private MockResponse json(String value) {
		return new MockResponse().setHeader("Content-Type", "application/json").setBody(value);
	}

	private RecordedRequest takeRequest(String path) throws Exception {
		RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
		assertThat(request).isNotNull();
		assertThat(request.getPath()).isEqualTo(path);
		assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-key");
		assertThat(request.getHeader("X-Client")).isEqualTo("shared");
		return request;
	}

	private JsonNode body(RecordedRequest request) throws Exception {
		return Json.OBJECT_MAPPER.readTree(request.getBody().readUtf8());
	}

}
