package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionRequest;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationRequest;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResult;
import okhttp3.Dispatcher;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.pigmesh.ai.deepseek.core.LogLevel.DEBUG;

public abstract class OpenAiClient {

    public SyncOrAsyncOrStreaming<CompletionResponse> completion(CompletionRequest request) {
        return completion(new OpenAiClientContext(), request);
    }

    public SyncOrAsyncOrStreaming<CompletionResponse> completion(
            OpenAiClientContext clientContext, CompletionRequest request) {
        throw new UnsupportedOperationException();
    }

    public SyncOrAsyncOrStreaming<String> completion(String prompt) {
        return completion(new OpenAiClientContext(), prompt);
    }

    public SyncOrAsyncOrStreaming<String> completion(OpenAiClientContext clientContext,
                                                     String prompt) {
        throw new UnsupportedOperationException();
    }

    public SyncOrAsyncOrStreaming<ChatCompletionResponse> chatCompletion(
            ChatCompletionRequest request) {
        return chatCompletion(new OpenAiClientContext(), request);
    }

    public SyncOrAsyncOrStreaming<ChatCompletionResponse> chatCompletion(
            OpenAiClientContext clientContext,
            ChatCompletionRequest request) {
        throw new UnsupportedOperationException();
    }

    public SyncOrAsyncOrStreaming<String> chatCompletion(String userMessage) {
        return chatCompletion(new OpenAiClientContext(), userMessage);
    }

    public SyncOrAsyncOrStreaming<String> chatCompletion(
            OpenAiClientContext clientContext,
            String userMessage) {
        throw new UnsupportedOperationException();
    }

    public Flux<ChatCompletionResponse> chatFluxCompletion(ChatCompletionRequest request) {
        throw new UnsupportedOperationException();
    }

    public Flux<ChatCompletionResponse> chatFluxCompletion(String userMessage) {
        throw new UnsupportedOperationException();
    }



    public SyncOrAsync<List<Float>> embedding(String input) {
        return embedding(new OpenAiClientContext(), input);
    }

    public SyncOrAsync<List<Float>> embedding(OpenAiClientContext clientContext,
                                              String input) {
        throw new UnsupportedOperationException();
    }

    public SyncOrAsync<ModerationResponse> moderation(ModerationRequest request) {
        return moderation(new OpenAiClientContext(), request);
    }

    public SyncOrAsync<ModerationResponse> moderation(OpenAiClientContext clientContext,
                                                      ModerationRequest request) {
        throw new UnsupportedOperationException();
    }

    public SyncOrAsync<ModerationResult> moderation(String input) {
        return moderation(new OpenAiClientContext(), input);
    }

    public SyncOrAsync<ModerationResult> moderation(OpenAiClientContext clientContext,
                                                    String input) {
        throw new UnsupportedOperationException();
    }


    public abstract void shutdown();

    @SuppressWarnings("rawtypes")
    public static DeepSeekClient.Builder builder() {
        // fallback to the default
        return DeepSeekClient.builder();
    }

    public static class OpenAiClientContext {
        private final Map<String, String> headers = new HashMap<>();

        public OpenAiClientContext addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public OpenAiClientContext addHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Map<String, String> headers() {
            return headers;
        }

        public static OpenAiClientContext create() {
            return new OpenAiClientContext();
        }
    }

    @SuppressWarnings("unchecked")
    public abstract static class Builder<T extends OpenAiClient, B extends Builder<T, B>> {

        public String baseUrl = "https://api.deepseek.com/v1/";
        public String organizationId;
        public String model;
        public String apiVersion;
        public String openAiApiKey;
        public String azureApiKey;
        public Duration callTimeout = Duration.ofSeconds(60);
        public Duration connectTimeout = Duration.ofSeconds(60);
        public Duration readTimeout = Duration.ofSeconds(60);
        public Duration writeTimeout = Duration.ofSeconds(60);
        public Dispatcher dispatcher;
        public Proxy proxy;
        public String userAgent;
        public boolean logRequests;
        public boolean logResponses;
        public LogLevel logLevel = DEBUG;
        public boolean logStreamingResponses;
        public Path persistTo;
        public Map<String, String> customHeaders;
        public String systemMessage;

        public abstract T build();

        /**
         * @param baseUrl Base URL of OpenAI API.
         *                For OpenAI (default): "https://api.openai.com/v1/"
         *                For Azure OpenAI: "https://{resource-name}.openai.azure.com/openai/deployments/{deployment-id}/"
         * @return builder
         */
        public B baseUrl(String baseUrl) {
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("baseUrl cannot be null or empty");
            }
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
            return (B) this;
        }

        /**
         * @param organizationId The organizationId for OpenAI: https://platform.openai.com/docs/api-reference/organization-optional
         * @return builder
         */
        public B organizationId(String organizationId) {
            this.organizationId = organizationId;
            return (B) this;
        }

        /**
         * @param apiVersion Version of the API in the YYYY-MM-DD format. Applicable only for Azure OpenAI.
         * @return builder
         */
        public B apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return (B) this;
        }

        /**
         * @param openAiApiKey OpenAI API key.
         *                     Will be injected in HTTP headers like this: "Authorization: Bearer ${openAiApiKey}"
         * @return builder
         */
        public B openAiApiKey(String openAiApiKey) {
            if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "ApiKey 不能为空，请访问 DeepSeek 开放平台获取: https://platform.deepseek.com/api_keys"
                );
            }
            this.openAiApiKey = openAiApiKey;
            return (B) this;
        }

        public B model(String model) {
            this.model = model;
            return (B) this;
        }

        public B systemMessage(String systemMessage) {
            this.systemMessage = systemMessage;
            return (B) this;
        }

        /**
         * @param azureApiKey Azure API key.
         *                    Will be injected in HTTP headers like this: "api-key: ${azureApiKey}"
         * @return builder
         */
        public B azureApiKey(String azureApiKey) {
            if (azureApiKey == null || azureApiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("azureApiKey cannot be null or empty");
            }
            this.azureApiKey = azureApiKey;
            return (B) this;
        }

        public B callTimeout(Duration callTimeout) {
            if (callTimeout == null) {
                throw new IllegalArgumentException("callTimeout cannot be null");
            }
            this.callTimeout = callTimeout;
            return (B) this;
        }

        public B connectTimeout(Duration connectTimeout) {
            if (connectTimeout == null) {
                throw new IllegalArgumentException("connectTimeout cannot be null");
            }
            this.connectTimeout = connectTimeout;
            return (B) this;
        }

        public B readTimeout(Duration readTimeout) {
            if (readTimeout == null) {
                throw new IllegalArgumentException("readTimeout cannot be null");
            }
            this.readTimeout = readTimeout;
            return (B) this;
        }

        public B writeTimeout(Duration writeTimeout) {
            if (writeTimeout == null) {
                throw new IllegalArgumentException("writeTimeout cannot be null");
            }
            this.writeTimeout = writeTimeout;
            return (B) this;
        }

        public B dispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return (B) this;
        }

        public B proxy(Proxy.Type type, String ip, int port) {
            this.proxy = new Proxy(type, new InetSocketAddress(ip, port));
            return (B) this;
        }

        public B proxy(Proxy proxy) {
            this.proxy = proxy;
            return (B) this;
        }

        public B userAgent(String userAgent) {
            this.userAgent = userAgent;
            return (B) this;
        }

        public B logRequests() {
            return logRequests(true);
        }

        public B logRequests(Boolean logRequests) {
            if (logRequests == null) {
                logRequests = false;
            }
            this.logRequests = logRequests;
            return (B) this;
        }

        public B logLevel(LogLevel logLevel) {
            if (logLevel == null) {
                logLevel = DEBUG;
            }
            this.logLevel = logLevel;
            return (B) this;
        }

        public B logResponses() {
            return logResponses(true);
        }

        public B logResponses(Boolean logResponses) {
            if (logResponses == null) {
                logResponses = false;
            }
            this.logResponses = logResponses;
            return (B) this;
        }

        public B logStreamingResponses() {
            return logStreamingResponses(true);
        }

        public B logStreamingResponses(Boolean logStreamingResponses) {
            if (logStreamingResponses == null) {
                logStreamingResponses = false;
            }
            this.logStreamingResponses = logStreamingResponses;
            return (B) this;
        }

        /**
         * Generated response will be persisted under <code>java.io.tmpdir</code>. Used with images generation for the moment only.
         * The URL within <code>dev.ai4j.openai4j.image.GenerateImagesResponse</code> will contain the URL to local images then.
         *
         * @return builder
         */
        public B withPersisting() {
            persistTo = Paths.get(System.getProperty("java.io.tmpdir"));
            return (B) this;
        }

        /**
         * Generated response will be persisted under provided path. Used with images generation for the moment only.
         * The URL within <code>dev.ai4j.openai4j.image.GenerateImagesResponse</code> will contain the URL to local images then.
         *
         * @param persistTo path
         * @return builder
         */
        public B persistTo(Path persistTo) {
            this.persistTo = persistTo;
            return (B) this;
        }

        /**
         * Custom headers to be added to each HTTP request.
         *
         * @param customHeaders a map of headers
         * @return builder
         */
        public B customHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return (B) this;
        }
    }
}
