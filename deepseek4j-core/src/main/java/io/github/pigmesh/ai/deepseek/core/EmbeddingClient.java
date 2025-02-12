package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.embedding.EmbeddingRequest;
import io.github.pigmesh.ai.deepseek.core.embedding.EmbeddingResponse;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 支持向量话的客户端
 *
 * @author lengleng
 * @date 2025/02/12
 */
public class EmbeddingClient extends OpenAiClient {

	private static final Logger log = LoggerFactory.getLogger(EmbeddingClient.class);

	private final String baseUrl;

	private final String model;

	private final String apiVersion;

	private final OkHttpClient okHttpClient;

	private final OpenAiApi openAiApi;

	private final boolean logStreamingResponses;

	public EmbeddingClient(String apiKey) {
		this(new Builder().openAiApiKey(apiKey));
	}

	private EmbeddingClient(Builder serviceBuilder) {
		this.baseUrl = serviceBuilder.baseUrl;
		this.apiVersion = serviceBuilder.apiVersion;
		this.model = serviceBuilder.model;

		OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().callTimeout(serviceBuilder.callTimeout)
				.connectTimeout(serviceBuilder.connectTimeout).readTimeout(serviceBuilder.readTimeout)
				.writeTimeout(serviceBuilder.writeTimeout);

		if (serviceBuilder.dispatcher != null) {
			okHttpClientBuilder.dispatcher(serviceBuilder.dispatcher);
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

	public static class Builder extends OpenAiClient.Builder<EmbeddingClient, Builder> {

		public EmbeddingClient build() {
			return new EmbeddingClient(this);
		}

	}

	public EmbeddingResponse embed(EmbeddingRequest request) {
		return this.embedding(new OpenAiClientContext(), request).execute();
	}

	public List<Float> embed(String input) {
		return this.embedding(new OpenAiClientContext(), input).execute();
	}

	@Override
	public SyncOrAsync<EmbeddingResponse> embedding(OpenAiClientContext context, EmbeddingRequest request) {

		if (Objects.isNull(request.getModel())) {
			request.setModel(this.model);
		}

		return new RequestExecutor<>(openAiApi.embeddings(context.headers(), request, apiVersion), r -> r);
	}

	@Override
	public SyncOrAsync<List<Float>> embedding(OpenAiClientContext context, String input) {
		EmbeddingRequest.Builder builder = EmbeddingRequest.builder().input(input);
		if (Objects.nonNull(this.model)) {
			builder.model(this.model);
		}

		return new RequestExecutor<>(openAiApi.embeddings(context.headers(), builder.build(), apiVersion),
				EmbeddingResponse::embedding);
	}

}
