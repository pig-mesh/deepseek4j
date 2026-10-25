package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionModel;
import io.github.pigmesh.ai.deepseek.core.chat.ReasoningEffort;
import io.github.pigmesh.ai.deepseek.core.chat.Thinking;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionRequest;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionResponse;
import io.github.pigmesh.ai.deepseek.core.models.ModelsResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationRequest;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResult;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchRequest;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchResponse;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private final String officialSearchPath;

	private final boolean logStreamingResponses;

	private final String systemMessage;

	private final Boolean thinkingEnabled;

	private final String reasoningEffort;

	public DeepSeekClient(String apiKey) {
		this(new Builder().openAiApiKey(apiKey));
	}

	public DeepSeekClient(Builder serviceBuilder) {
		this.baseUrl = serviceBuilder.baseUrl;
		this.apiVersion = serviceBuilder.apiVersion;
		this.model = serviceBuilder.model;
		this.systemMessage = serviceBuilder.systemMessage;
		this.thinkingEnabled = serviceBuilder.thinkingEnabled;
		this.reasoningEffort = serviceBuilder.reasoningEffort;

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

		Retrofit retrofit = retrofitBuilder.build();
		this.openAiApi = retrofit.create(OpenAiApi.class);
		this.officialSearchPath = "/".equals(retrofit.baseUrl().encodedPath()) ? "v1/tools/search" : "tools/search";

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

		private Boolean thinkingEnabled = true;

		private String reasoningEffort = ReasoningEffort.HIGH.getValue();

		public Builder() {
			this.model = ChatCompletionModel.DEEPSEEK_FLASH.getValue();
		}

		public Builder thinkingEnabled(Boolean thinkingEnabled) {
			this.thinkingEnabled = thinkingEnabled;
			return this;
		}

		public Builder reasoningEffort(ReasoningEffort reasoningEffort) {
			return reasoningEffort(reasoningEffort == null ? null : reasoningEffort.getValue());
		}

		public Builder reasoningEffort(String reasoningEffort) {
			this.reasoningEffort = ReasoningEffort.normalize(reasoningEffort);
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

		ChatCompletionRequest effectiveRequest = applyChatDefaults(request);

		ChatCompletionRequest syncRequest = ChatCompletionRequest.builder().from(effectiveRequest).stream(false)
				.build();

		return new RequestExecutor<>(openAiApi.chatCompletions(context.headers(), syncRequest, apiVersion), r -> r,
				okHttpClient, formatUrl("chat/completions"),
				() -> ChatCompletionRequest.builder().from(effectiveRequest).stream(true).build(),
				ChatCompletionResponse.class, r -> r, logStreamingResponses);
	}

	@Override
	public SyncOrAsyncOrStreaming<String> chatCompletion(OpenAiClientContext context, String userMessage) {
		ChatCompletionRequest request = applyChatDefaults(
				ChatCompletionRequest.builder().addUserMessage(userMessage).build());

		ChatCompletionRequest syncRequest = ChatCompletionRequest.builder().from(request).stream(false).build();

		return new RequestExecutor<>(openAiApi.chatCompletions(context.headers(), syncRequest, apiVersion),
				ChatCompletionResponse::content, okHttpClient, formatUrl("chat/completions"),
				() -> ChatCompletionRequest.builder().from(request).stream(true).build(), ChatCompletionResponse.class,
				r -> r.choices().get(0).delta().content(), logStreamingResponses);
	}

	@Override
	public Flux<ChatCompletionResponse> chatFluxCompletion(ChatCompletionRequest request) {
		return Flux.create(emitter -> {
			ResponseHandle handle = this.chatCompletion(new OpenAiClientContext(), request)
					.onPartialResponse(emitter::next).onComplete(emitter::complete).onError(emitter::error).execute();
			emitter.onDispose(handle::cancel);
		});
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
		ChatCompletionRequest request = applyChatDefaults(builder.build());
		return chatFluxCompletion(request);
	}

	/**
	 * 先使用官方 Search 搜索，再流式生成回答。默认搜索 5 条摘要，复用客户端配置。
	 * @param userMessage 问题，同时用作搜索 query，须为 1～128 个非全空白 Unicode 字符；长问题请使用显式搜索参数重载
	 * @return 订阅时发起搜索的冷流，取消订阅会取消当前 HTTP 请求
	 */
	public Flux<ChatCompletionResponse> chatSearchCompletion(String userMessage) {
		return chatSearchCompletion(userMessage,
				DeepSeekSearchRequest.builder().query(userMessage).count(5).needContent(false).build());
	}

	/**
	 * 使用独立的搜索 query 为问题生成流式回答，问题原文不会被截断。
	 * @param userMessage 完整问题
	 * @param searchRequest 官方搜索参数
	 * @return 搜索结果为空时正常聊天；搜索失败时通过 onError 返回错误
	 */
	public Flux<ChatCompletionResponse> chatSearchCompletion(String userMessage, DeepSeekSearchRequest searchRequest) {
		return chatSearchCompletion(ChatCompletionRequest.builder().addUserMessage(userMessage).build(), searchRequest);
	}

	/**
	 * 搜索并生成流式回答，保留历史消息和单次请求参数，未设置的模型及思考参数沿用客户端默认值。 没有 system 消息时补充客户端
	 * systemMessage；搜索资料作为 user 消息插入最后一条 user 消息之前。
	 * @param request 对话请求，至少包含一条 user 消息
	 * @param searchRequest 官方搜索参数，query 须为 1～128 个非全空白 Unicode 字符
	 * @return 每次订阅独立执行搜索和对话，取消订阅会取消当前 HTTP 请求
	 */
	public Flux<ChatCompletionResponse> chatSearchCompletion(ChatCompletionRequest request,
			DeepSeekSearchRequest searchRequest) {
		return Flux.defer(() -> {
			SearchChatContext.validate(request, searchRequest);
			return searchForChat(searchRequest).flatMapMany(response -> chatFluxCompletion(
					SearchChatContext.apply(request, searchRequest, response, systemMessage)));
		});
	}

	private Mono<DeepSeekSearchResponse> searchForChat(DeepSeekSearchRequest request) {
		return Mono.create(sink -> {
			Call<DeepSeekSearchResponse> call = createSearchCall(new OpenAiClientContext(), request);
			AtomicBoolean cancelled = new AtomicBoolean();
			sink.onCancel(() -> {
				cancelled.set(true);
				call.cancel();
			});
			new AsyncRequestExecutor<>(call, response -> response).onResponse(response -> {
				if (cancelled.get()) {
					return;
				}
				if (response == null) {
					sink.error(new IllegalStateException("DeepSeek Search returned an empty response body"));
				}
				else {
					sink.success(response);
				}
			}, error -> {
				if (!cancelled.get()) {
					sink.error(error);
				}
			});
		});
	}

	/**
	 * 调用 DeepSeek 官方 Search API，使用客户端的 DeepSeek API Key 和 baseUrl。
	 * @param request 搜索参数
	 * @return 支持同步执行或异步回调的搜索请求
	 */
	public SyncOrAsync<DeepSeekSearchResponse> search(DeepSeekSearchRequest request) {
		return search(new OpenAiClientContext(), request);
	}

	/**
	 * 调用 DeepSeek 官方 Search API，并附加本次请求的 headers。
	 * @param context 请求上下文
	 * @param request 搜索参数
	 * @return 支持同步执行或异步回调的搜索请求
	 */
	public SyncOrAsync<DeepSeekSearchResponse> search(OpenAiClientContext context, DeepSeekSearchRequest request) {
		return new RequestExecutor<>(createSearchCall(context, request), r -> r);
	}

	private Call<DeepSeekSearchResponse> createSearchCall(OpenAiClientContext context, DeepSeekSearchRequest request) {
		return openAiApi.search(officialSearchPath, context.headers(), request);
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

	private ChatCompletionRequest applyChatDefaults(ChatCompletionRequest request) {
		ChatCompletionRequest.Builder builder = ChatCompletionRequest.builder().from(request);
		if (Objects.isNull(request.model()) && Objects.nonNull(this.model)) {
			builder.model(this.model);
		}
		Thinking effectiveThinking = request.thinking();
		if (Objects.isNull(effectiveThinking) && Objects.nonNull(this.thinkingEnabled)) {
			effectiveThinking = Thinking.of(this.thinkingEnabled);
			builder.thinking(effectiveThinking);
		}
		if (Objects.isNull(request.reasoningEffort()) && isThinkingEnabled(effectiveThinking)
				&& Objects.nonNull(this.reasoningEffort)) {
			builder.reasoningEffort(this.reasoningEffort);
		}
		return builder.build();
	}

	private boolean isThinkingEnabled(Thinking thinking) {
		return thinking != null && "enabled".equals(thinking.type());
	}

}
