package io.github.pigmesh.ai.deepseek.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchRequest;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchResponse;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchResult;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepSeekSearchClientTest {

	private MockWebServer server;

	private DeepSeekClient client;

	@BeforeEach
	void setUp() {
		server = new MockWebServer();
		client = DeepSeekClient.builder().baseUrl(server.url("/v1/").toString()).openAiApiKey("deepseek-key").build();
	}

	@AfterEach
	void tearDown() throws Exception {
		try {
			client.shutdown();
		}
		finally {
			server.close();
		}
	}

	@Test
	void sendsOfficialSearchParametersAndMapsResults() throws Exception {
		server.enqueue(jsonResponse("{\"results\":[{\"rerank_score\":2.5,\"title\":\"DeepSeek API\","
				+ "\"url\":\"https://api-docs.deepseek.com/\",\"snippet\":\"short\",\"summary\":\"long\","
				+ "\"content\":\"body\",\"site_name\":\"DeepSeek\",\"site_icon\":\"https://example.com/icon.png\","
				+ "\"date\":\"2026-10-25T00:00:00Z\",\"authority\":4,\"future_field\":true},"
				+ "{\"rerank_score\":0,\"title\":\"Other\",\"url\":\"https://example.com/\",\"snippet\":\"\","
				+ "\"summary\":\"\",\"content\":\"\",\"site_name\":null,\"site_icon\":null,\"date\":null,"
				+ "\"authority\":-1}],\"future_field\":true}"));
		DeepSeekSearchRequest request = DeepSeekSearchRequest.builder().query("DeepSeek Search").count(5)
				.oriQuestion("如何调用 DeepSeek 搜索？").localization("1.2.3.4").needContent(true).build();
		OpenAiClient.OpenAiClientContext context = OpenAiClient.OpenAiClientContext.create().addHeader("X-Request-Id",
				"search-test");

		DeepSeekSearchResponse response = client.search(context, request).execute();

		RecordedRequest recorded = server.takeRequest(5, TimeUnit.SECONDS);
		assertThat(recorded).isNotNull();
		assertThat(recorded.getMethod()).isEqualTo("POST");
		assertThat(recorded.getPath()).isEqualTo("/v1/tools/search");
		assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer deepseek-key");
		assertThat(recorded.getHeader("Content-Type")).startsWith("application/json");
		assertThat(recorded.getHeader("X-Request-Id")).isEqualTo("search-test");
		JsonNode body = Json.OBJECT_MAPPER.readTree(recorded.getBody().readUtf8());
		assertThat(body).isEqualTo(Json.OBJECT_MAPPER.readTree("{\"query\":\"DeepSeek Search\",\"count\":5,"
				+ "\"ori_question\":\"如何调用 DeepSeek 搜索？\",\"localization\":\"1.2.3.4\",\"need_content\":true}"));
		assertThat(response.results()).hasSize(2);
		DeepSeekSearchResult first = response.results().get(0);
		assertThat(first.rerankScore()).isEqualTo(2.5);
		assertThat(first.title()).isEqualTo("DeepSeek API");
		assertThat(first.url()).isEqualTo("https://api-docs.deepseek.com/");
		assertThat(first.snippet()).isEqualTo("short");
		assertThat(first.summary()).isEqualTo("long");
		assertThat(first.content()).isEqualTo("body");
		assertThat(first.siteName()).isEqualTo("DeepSeek");
		assertThat(first.siteIcon()).isEqualTo("https://example.com/icon.png");
		assertThat(first.date()).isEqualTo("2026-10-25T00:00:00Z");
		assertThat(first.authority()).isEqualTo(4);
		DeepSeekSearchResult second = response.results().get(1);
		assertThat(second.rerankScore()).isZero();
		assertThat(second.content()).isEmpty();
		assertThat(second.siteName()).isNull();
		assertThat(second.siteIcon()).isNull();
		assertThat(second.date()).isNull();
		assertThat(second.authority()).isEqualTo(-1);
	}

	@ParameterizedTest
	@CsvSource({ "/, /v1/tools/search, /chat/completions", "/v1/, /v1/tools/search, /v1/chat/completions",
			"/gateway/deepseek/, /gateway/deepseek/tools/search, /gateway/deepseek/chat/completions",
			"/gateway/deepseek/v1/, /gateway/deepseek/v1/tools/search, /gateway/deepseek/v1/chat/completions" })
	void resolvesSearchPathWithoutChangingChatPath(String basePath, String searchPath, String chatPath)
			throws Exception {
		client.shutdown();
		client = DeepSeekClient.builder().baseUrl(server.url(basePath).toString()).openAiApiKey("deepseek-key").build();
		server.enqueue(jsonResponse("{\"results\":[]}"));
		server.enqueue(
				jsonResponse("{\"id\":\"chatcmpl-test\",\"created\":1,\"model\":\"deepseek-flash\",\"choices\":[]}"));

		client.search(DeepSeekSearchRequest.builder().query("DeepSeek").build()).execute();
		client.chatCompletion(ChatCompletionRequest.builder().addUserMessage("hello").build()).execute();

		RecordedRequest search = server.takeRequest(5, TimeUnit.SECONDS);
		RecordedRequest chat = server.takeRequest(5, TimeUnit.SECONDS);
		assertThat(search).isNotNull();
		assertThat(chat).isNotNull();
		assertThat(search.getPath()).isEqualTo(searchPath);
		assertThat(chat.getPath()).isEqualTo(chatPath);
	}

	@Test
	void omitsOptionalParametersAndPreservesQuerySyntax() throws Exception {
		server.enqueue(jsonResponse("{\"results\":[]}"));
		String query = "site:example.com \"搜索🔎\" -广告 after:2026-01-01";
		DeepSeekSearchRequest request = DeepSeekSearchRequest.builder().query(query).oriQuestion(null).build();

		DeepSeekSearchResponse response = client.search(request).execute();

		RecordedRequest recorded = server.takeRequest(5, TimeUnit.SECONDS);
		assertThat(recorded).isNotNull();
		JsonNode body = Json.OBJECT_MAPPER.readTree(recorded.getBody().readUtf8());
		assertThat(body.size()).isEqualTo(1);
		assertThat(body.path("query").asText()).isEqualTo(query);
		assertThat(response.results()).isEmpty();
		assertThat(DeepSeekSearchResponse.builder().build()).isEqualTo(response);
	}

	@Test
	void sendsExplicitFalseForNeedContent() throws Exception {
		server.enqueue(jsonResponse("{\"results\":[]}"));

		client.search(DeepSeekSearchRequest.builder().query("DeepSeek").needContent(false).build()).execute();

		RecordedRequest recorded = server.takeRequest(5, TimeUnit.SECONDS);
		assertThat(recorded).isNotNull();
		assertThat(Json.OBJECT_MAPPER.readTree(recorded.getBody().readUtf8()))
				.isEqualTo(Json.OBJECT_MAPPER.readTree("{\"query\":\"DeepSeek\",\"need_content\":false}"));
	}

	@Test
	void deserializesSnakeCaseRequestParameters() throws Exception {
		String json = "{\"query\":\"DeepSeek\",\"count\":50,\"ori_question\":\"原始问题\","
				+ "\"localization\":\"1.2.3.4\",\"need_content\":false}";

		DeepSeekSearchRequest request = Json.OBJECT_MAPPER.readValue(json, DeepSeekSearchRequest.class);

		assertThat(request.query()).isEqualTo("DeepSeek");
		assertThat(request.count()).isEqualTo(50);
		assertThat(request.oriQuestion()).isEqualTo("原始问题");
		assertThat(request.localization()).isEqualTo("1.2.3.4");
		assertThat(request.needContent()).isFalse();
		assertThat(Json.OBJECT_MAPPER.readTree(Json.OBJECT_MAPPER.writeValueAsString(request)))
				.isEqualTo(Json.OBJECT_MAPPER.readTree(json));
	}

	@Test
	void preservesHttpErrors() {
		String error = "{\"error\":{\"message\":\"Invalid query\",\"type\":\"invalid_request_error\",\"param\":null}}";
		server.enqueue(jsonResponse(error).setResponseCode(400));

		assertThatThrownBy(() -> client.search(DeepSeekSearchRequest.builder().query("").build()).execute())
				.isInstanceOfSatisfying(OpenAiHttpException.class, exception -> {
					assertThat(exception.code()).isEqualTo(400);
					assertThat(exception.getMessage()).isEqualTo(error);
				});
	}

	@Test
	void supportsAsyncSearch() throws Exception {
		server.enqueue(jsonResponse("{\"results\":[]}"));
		CompletableFuture<DeepSeekSearchResponse> response = new CompletableFuture<>();

		client.search(DeepSeekSearchRequest.builder().query("DeepSeek").build()).onResponse(response::complete,
				response::completeExceptionally);

		assertThat(response.get(5, TimeUnit.SECONDS).results()).isEmpty();
		RecordedRequest recorded = server.takeRequest(5, TimeUnit.SECONDS);
		assertThat(recorded).isNotNull();
		assertThat(recorded.getPath()).isEqualTo("/v1/tools/search");
	}

	@Test
	void forwardsAsyncHttpErrors() {
		server.enqueue(jsonResponse("{\"error\":{\"message\":\"Rate limit exceeded\"}}").setResponseCode(429));
		CompletableFuture<DeepSeekSearchResponse> response = new CompletableFuture<>();

		client.search(DeepSeekSearchRequest.builder().query("DeepSeek").build()).onResponse(response::complete,
				response::completeExceptionally);

		assertThatThrownBy(() -> response.get(5, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class).cause()
				.isInstanceOfSatisfying(OpenAiHttpException.class,
						exception -> assertThat(exception.code()).isEqualTo(429));
	}

	private MockResponse jsonResponse(String body) {
		return new MockResponse().setHeader("Content-Type", "application/json").setBody(body);
	}

}
