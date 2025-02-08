package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionRequest;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationRequest;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResult;
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

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .callTimeout(serviceBuilder.callTimeout)
                .connectTimeout(serviceBuilder.connectTimeout)
                .readTimeout(serviceBuilder.readTimeout)
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
        } else {
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
    }

    public void shutdown() {
        okHttpClient.dispatcher().executorService().shutdown();

        okHttpClient.connectionPool().evictAll();

        Cache cache = okHttpClient.cache();
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException e) {
                log.error("Failed to close cache", e);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends OpenAiClient.Builder<DeepSeekClient, Builder> {

        public DeepSeekClient build() {
            return new DeepSeekClient(this);
        }
    }

    @Override
    public SyncOrAsyncOrStreaming<CompletionResponse> completion(OpenAiClient.OpenAiClientContext context,
                                                                 CompletionRequest request) {
        CompletionRequest syncRequest = CompletionRequest.builder().from(request).stream(false).build();

        return new RequestExecutor<>(
                openAiApi.completions(context.headers(), syncRequest, apiVersion),
                r -> r,
                okHttpClient,
                formatUrl("completions"),
                () -> CompletionRequest.builder().from(request).stream(true).build(),
                CompletionResponse.class,
                r -> r,
                logStreamingResponses
        );
    }

    @Override
    public SyncOrAsyncOrStreaming<String> completion(OpenAiClient.OpenAiClientContext context, String prompt) {
        CompletionRequest request = CompletionRequest.builder().prompt(prompt).build();

        CompletionRequest syncRequest = CompletionRequest.builder().from(request).stream(false).build();

        return new RequestExecutor<>(
                openAiApi.completions(context.headers(), syncRequest, apiVersion),
                CompletionResponse::text,
                okHttpClient,
                formatUrl("completions"),
                () -> CompletionRequest.builder().from(request).stream(true).build(),
                CompletionResponse.class,
                CompletionResponse::text,
                logStreamingResponses
        );
    }

    @Override
    public SyncOrAsyncOrStreaming<ChatCompletionResponse> chatCompletion(OpenAiClient.OpenAiClientContext context,
                                                                         ChatCompletionRequest request) {
        ChatCompletionRequest syncRequest = ChatCompletionRequest.builder().from(request).stream(false).build();

        return new RequestExecutor<>(
                openAiApi.chatCompletions(context.headers(), syncRequest, apiVersion),
                r -> r,
                okHttpClient,
                formatUrl("chat/completions"),
                () -> ChatCompletionRequest.builder().from(request).stream(true).build(),
                ChatCompletionResponse.class,
                r -> r,
                logStreamingResponses
        );
    }

    @Override
    public SyncOrAsyncOrStreaming<String> chatCompletion(OpenAiClient.OpenAiClientContext context, String userMessage) {
        ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage(userMessage).build();

        ChatCompletionRequest syncRequest = ChatCompletionRequest.builder().from(request).stream(false).build();

        return new RequestExecutor<>(
                openAiApi.chatCompletions(context.headers(), syncRequest, apiVersion),
                ChatCompletionResponse::content,
                okHttpClient,
                formatUrl("chat/completions"),
                () -> ChatCompletionRequest.builder().from(request).stream(true).build(),
                ChatCompletionResponse.class,
                r -> r.choices().get(0).delta().content(),
                logStreamingResponses
        );
    }

    @Override
    public Flux<ChatCompletionResponse> chatFluxCompletion(ChatCompletionRequest request) {
        if (Objects.nonNull(this.model)) {
            request = ChatCompletionRequest.builder().from(request).model(this.model).build();
        }

        ChatCompletionRequest finalRequest = request;
        return Flux.create(emitter -> {
            this.chatCompletion(new OpenAiClientContext(), finalRequest).onPartialResponse(emitter::next)
                    .onComplete(emitter::complete)
                    .onError(emitter::error)
                    .execute();
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
        return Flux.create(emitter -> {
            this.chatCompletion(new OpenAiClientContext(), builder.build()).onPartialResponse(emitter::next)
                    .onComplete(emitter::complete)
                    .onError(emitter::error)
                    .execute();
        });
    }


    @Override
    public SyncOrAsync<ModerationResponse> moderation(OpenAiClient.OpenAiClientContext context,
                                                      ModerationRequest request) {
        return new RequestExecutor<>(openAiApi.moderations(context.headers(), request, apiVersion),
                r -> r);
    }

    @Override
    public SyncOrAsync<ModerationResult> moderation(OpenAiClient.OpenAiClientContext context, String input) {
        ModerationRequest request = ModerationRequest.builder().input(input).build();

        return new RequestExecutor<>(openAiApi.moderations(context.headers(), request, apiVersion),
                r -> r.results().get(0));
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
