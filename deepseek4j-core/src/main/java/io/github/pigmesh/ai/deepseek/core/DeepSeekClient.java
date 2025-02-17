package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionRequest;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionResponse;
import io.github.pigmesh.ai.deepseek.core.models.ModelsResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationRequest;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResult;
import io.github.pigmesh.ai.deepseek.core.search.SearchApi;
import io.github.pigmesh.ai.deepseek.core.search.SearchRequest;
import io.github.pigmesh.ai.deepseek.core.search.SearchResponse;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Deep Seek 客户端
 *
 * @author lengleng
 * @date 2025/02/06
 */
public class DeepSeekClient extends OpenAiClient {

	private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

	private final String baseUrl;

	private final String apiVersion;

	private final String model;

	private final OkHttpClient okHttpClient;

	private final OpenAiApi openAiApi;

	private final SearchApi searchApi;

	private final boolean logStreamingResponses;

	private final String systemMessage;

	public DeepSeekClient(String apiKey) {
		this(new Builder().openAiApiKey(apiKey));
	}

	public DeepSeekClient(Builder serviceBuilder) {
		this.baseUrl = serviceBuilder.baseUrl;
		this.apiVersion = serviceBuilder.apiVersion;
		this.model = serviceBuilder.model;
		this.systemMessage = serviceBuilder.systemMessage;

		OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().callTimeout(serviceBuilder.callTimeout)
				.connectTimeout(serviceBuilder.connectTimeout).readTimeout(serviceBuilder.readTimeout)
				.writeTimeout(serviceBuilder.writeTimeout);

		if (serviceBuilder.dispatcher != null) {
			okHttpClientBuilder.dispatcher(serviceBuilder.dispatcher);
		}

		if (serviceBuilder.openAiApiKey == null && serviceBuilder.azureApiKey == null) {
			throw new IllegalArgumentException("openAiApiKey OR azureApiKey must be defined");
		}
		if (serviceBuilder.openAiApiKey != null && serviceBuilder.azureApiKey != null) {
			throw new IllegalArgumentException("openAiApiKey AND azureApiKey cannot both be defined at the same time");
		}
		if (serviceBuilder.openAiApiKey != null) {
			okHttpClientBuilder.addInterceptor(new AuthorizationHeaderInjector(serviceBuilder.openAiApiKey));
		}
		else {
			okHttpClientBuilder.addInterceptor(new ApiKeyHeaderInjector(serviceBuilder.azureApiKey));
		}

		Map<String, String> headers = new HashMap<>();
		if (serviceBuilder.organizationId != null) {
			headers.put("OpenAI-Organization", serviceBuilder.organizationId);
		}
		if (serviceBuilder.userAgent != null) {
			headers.put("User-Agent", serviceBuilder.userAgent);
		}
		if (serviceBuilder.customHeaders != null) {
			headers.putAll(serviceBuilder.customHeaders);
		}
		if (!headers.isEmpty()) {
			okHttpClientBuilder.addInterceptor(new GenericHeaderInjector(headers));
		}

		if (serviceBuilder.proxy != null) {
			okHttpClientBuilder.proxy(serviceBuilder.proxy);
		}

		if (serviceBuilder.logRequests) {
			okHttpClientBuilder.addInterceptor(new RequestLoggingInterceptor(serviceBuilder.logLevel));
		}

		if (serviceBuilder.logResponses) {
			okHttpClientBuilder.addInterceptor(new ResponseLoggingInterceptor(serviceBuilder.logLevel));
		}
		this.logStreamingResponses = serviceBuilder.logStreamingResponses;

		this.okHttpClient = okHttpClientBuilder.build();

		Retrofit.Builder retrofitBuilder = new Retrofit.Builder().baseUrl(serviceBuilder.baseUrl).client(okHttpClient);

		if (serviceBuilder.persistTo != null) {
			retrofitBuilder.addConverterFactory(new PersistorConverterFactory(serviceBuilder.persistTo));
		}

		retrofitBuilder.addConverterFactory(JacksonConverterFactory.create(Json.OBJECT_MAPPER));

		this.openAiApi = retrofitBuilder.build().create(OpenAiApi.class);

		// Search API
		OkHttpClient searchOkHttpClient = new OkHttpClient.Builder().callTimeout(serviceBuilder.callTimeout)
				.connectTimeout(serviceBuilder.connectTimeout).readTimeout(serviceBuilder.readTimeout)
				.writeTimeout(serviceBuilder.writeTimeout)
				.addInterceptor(new RequestLoggingInterceptor(serviceBuilder.logLevel))
				.addInterceptor(new ResponseLoggingInterceptor(serviceBuilder.logLevel))
				.addInterceptor(new AuthorizationHeaderInjector(serviceBuilder.searchApiKey)).build();
		Retrofit.Builder searchRetrofitBuilder = new Retrofit.Builder().baseUrl(serviceBuilder.searchEndpoint)
				.client(searchOkHttpClient);
		searchRetrofitBuilder.addConverterFactory(JacksonConverterFactory.create(Json.OBJECT_MAPPER));
		this.searchApi = searchRetrofitBuilder.build().create(SearchApi.class);
	}

	public void shutdown() {
		okHttpClient.dispatcher().executorService().shutdown();

		okHttpClient.connectionPool().evictAll();

		Cache cache = okHttpClient.cache();
		if (cache != null) {
			try {
				cache.close();
			}
			catch (IOException e) {
				log.error("Failed to close cache", e);
			}
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder extends OpenAiClient.Builder<DeepSeekClient, Builder> {

		private String searchEndpoint = "https://api.bochaai.com/v1/";

		private String searchApiKey;

		private SearchApi searchApi;

		public Builder searchApi(SearchApi searchApi) {
			this.searchApi = searchApi;
			return this;
		}

		public Builder searchApiKey(String searchApiKey) {
			this.searchApiKey = searchApiKey;
			return this;
		}

		public Builder searchEndpoint(String searchEndpoint) {
			this.searchEndpoint = searchEndpoint;
			return this;
		}

		public DeepSeekClient build() {
			return new DeepSeekClient(this);
		}

	}

	@Override
	public SyncOrAsyncOrStreaming<CompletionResponse> completion(OpenAiClientContext context,
			CompletionRequest request) {
		CompletionRequest syncRequest = CompletionRequest.builder().from(request).stream(false).build();

		return new RequestExecutor<>(openAiApi.completions(context.headers(), syncRequest, apiVersion), r -> r,
				okHttpClient, formatUrl("completions"),
				() -> CompletionRequest.builder().from(request).stream(true).build(), CompletionResponse.class, r -> r,
				logStreamingResponses);
	}

	@Override
	public SyncOrAsyncOrStreaming<String> completion(OpenAiClientContext context, String prompt) {
		CompletionRequest request = CompletionRequest.builder().prompt(prompt).build();

		CompletionRequest syncRequest = CompletionRequest.builder().from(request).stream(false).build();

		return new RequestExecutor<>(openAiApi.completions(context.headers(), syncRequest, apiVersion),
				CompletionResponse::text, okHttpClient, formatUrl("completions"),
				() -> CompletionRequest.builder().from(request).stream(true).build(), CompletionResponse.class,
				CompletionResponse::text, logStreamingResponses);
	}

	@Override
	public SyncOrAsyncOrStreaming<ChatCompletionResponse> chatCompletion(OpenAiClientContext context,
			ChatCompletionRequest request) {

		if (Objects.isNull(request.getModel())) {
			request.setModel(this.model);
		}

		ChatCompletionRequest syncRequest = ChatCompletionRequest.builder().from(request).stream(false).build();

		return new RequestExecutor<>(openAiApi.chatCompletions(context.headers(), syncRequest, apiVersion), r -> r,
				okHttpClient, formatUrl("chat/completions"),
				() -> ChatCompletionRequest.builder().from(request).stream(true).build(), ChatCompletionResponse.class,
				r -> r, logStreamingResponses);
	}

	@Override
	public SyncOrAsyncOrStreaming<String> chatCompletion(OpenAiClientContext context, String userMessage) {
		ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage(userMessage).build();

		ChatCompletionRequest syncRequest = ChatCompletionRequest.builder().from(request).stream(false).build();

		return new RequestExecutor<>(openAiApi.chatCompletions(context.headers(), syncRequest, apiVersion),
				ChatCompletionResponse::content, okHttpClient, formatUrl("chat/completions"),
				() -> ChatCompletionRequest.builder().from(request).stream(true).build(), ChatCompletionResponse.class,
				r -> r.choices().get(0).delta().content(), logStreamingResponses);
	}

	@Override
	public Flux<ChatCompletionResponse> chatFluxCompletion(ChatCompletionRequest request) {
		if (Objects.isNull(request.getModel())) {
			request.setModel(this.model);
		}

		return Flux.create(emitter -> {
			this.chatCompletion(new OpenAiClientContext(), request).onPartialResponse(emitter::next)
					.onComplete(emitter::complete).onError(emitter::error).execute();
		});
	}

	/**
	 * 聊天搜索
	 * @param userMessage 用户消息
	 * @return {@link Flux }<{@link ChatCompletionResponse }>
	 */
	public Flux<ChatCompletionResponse> chatSearchCompletion(String userMessage) {
		SearchResponse searchResponse = new SyncRequestExecutor<>(
				searchApi.webSearch(SearchRequest.builder().enable(true).query(userMessage).build()),
				searchResponse1 -> searchResponse1).execute();
		if (200 == searchResponse.getCode()) {
			String formatted = Utils.format(userMessage, searchResponse.getData().getWebPages().getValue());
			if (Objects.nonNull(formatted)) {
				return this.chatFluxCompletion(formatted);
			}
			return this.chatFluxCompletion(userMessage);
		}

		return this.chatFluxCompletion(userMessage);
	}

	/**
	 * 聊天搜索
	 * @param userMessage 用户消息
	 * @return {@link Flux }<{@link ChatCompletionResponse }>
	 */
	public SyncOrAsyncOrStreaming<ChatCompletionResponse> chatSearchStreamingCompletion(String userMessage) {
		SearchResponse searchResponse = new SyncRequestExecutor<>(
				searchApi.webSearch(SearchRequest.builder().enable(true).query(userMessage).build()),
				searchResponse1 -> searchResponse1).execute();
		if (200 == searchResponse.getCode()) {
			String formatted = Utils.format(userMessage, searchResponse.getData().getWebPages().getValue());
			if (Objects.nonNull(formatted)) {
				return this.chatCompletion(new OpenAiClientContext(), ChatCompletionRequest.builder().stream(true)
						.model(this.model).addUserMessage(formatted).build());
			}
		}
		return this.chatCompletion(new OpenAiClientContext(),
				ChatCompletionRequest.builder().stream(true).model(this.model).addUserMessage(userMessage).build());
	}

	/**
	 * 聊天搜索完成
	 * @param userMessage 用户消息
	 * @param searchRequest 搜索请求
	 * @return {@link Flux }<{@link ChatCompletionResponse }>
	 */
	public Flux<ChatCompletionResponse> chatSearchCompletion(String userMessage, SearchRequest searchRequest) {

		if (Objects.isNull(searchRequest.getQuery())) {
			searchRequest.setQuery(userMessage);
		}

		SearchResponse searchResponse = new SyncRequestExecutor<>(searchApi.webSearch(searchRequest),
				searchResponse1 -> searchResponse1).execute();
		if (200 == searchResponse.getCode()) {
			String formatted = Utils.format(userMessage, searchResponse.getData().getWebPages().getValue());
			if (Objects.nonNull(formatted)) {
				return this.chatFluxCompletion(formatted);
			}
			return this.chatFluxCompletion(userMessage);
		}

		return this.chatFluxCompletion(userMessage);
	}

	@Override
	public Flux<ChatCompletionResponse> chatFluxCompletion(String userMessage) {
		ChatCompletionRequest.Builder builder = ChatCompletionRequest.builder();

		if (Objects.nonNull(this.systemMessage)) {
			builder.addSystemMessage(this.systemMessage);
		}

		if (Objects.nonNull(this.model)) {
			builder.model(this.model);
		}

		builder.addUserMessage(userMessage);
		return Flux.create(emitter -> {
			this.chatCompletion(new OpenAiClientContext(), builder.build()).onPartialResponse(emitter::next)
					.onComplete(emitter::complete).onError(emitter::error).execute();
		});
	}

	@Override
	public SyncOrAsync<ModerationResponse> moderation(OpenAiClientContext context, ModerationRequest request) {
		return new RequestExecutor<>(openAiApi.moderations(context.headers(), request, apiVersion), r -> r);
	}

	@Override
	public SyncOrAsync<ModerationResult> moderation(OpenAiClientContext context, String input) {
		ModerationRequest request = ModerationRequest.builder().input(input).build();

		return new RequestExecutor<>(openAiApi.moderations(context.headers(), request, apiVersion),
				r -> r.results().get(0));
	}

	public ModelsResponse models() {

		return new RequestExecutor<>(this.openAiApi.models(new HashMap<>(), apiVersion), r -> r, okHttpClient, null,
				null, ModelsResponse.class, null, logStreamingResponses).execute();
	}

	private String formatUrl(String endpoint) {
		return baseUrl + endpoint + apiVersionQueryParam();
	}

	private String apiVersionQueryParam() {
		if (apiVersion == null || apiVersion.trim().isEmpty()) {
			return "";
		}
		return "?api-version=" + apiVersion;
	}

}
