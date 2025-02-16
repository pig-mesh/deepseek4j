package io.github.pigmesh.ai.deepseek.solon;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.EmbeddingClient;
import lombok.SneakyThrows;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.util.ResourceUtil;

import java.time.Duration;
import java.util.Objects;

/**
 * 装配 DeepSeekClient 和 EmbeddingClient
 *
 * @author songyinyin
 * @since 2025/2/16 16:11
 */
@Configuration
public class AutoConfiguration {

	@Bean
	@SneakyThrows
	@Condition(onMissingBean = DeepSeekClient.class, onProperty = "deepseek.api-key")
	public DeepSeekClient deepSeekClient(DeepSeekProperties deepSeekProperties) {

		DeepSeekClient.Builder builder = DeepSeekClient.builder().baseUrl(deepSeekProperties.getBaseUrl())
				.model(deepSeekProperties.getModel()).openAiApiKey(deepSeekProperties.getApiKey())
				.logRequests(deepSeekProperties.isLogRequests()).logResponses(deepSeekProperties.isLogResponses());

		if (Objects.nonNull(deepSeekProperties.getProxy())) {
			builder.proxy(deepSeekProperties.getProxy());
		}

		if (Objects.nonNull(deepSeekProperties.getConnectTimeout())) {
			builder.connectTimeout(Duration.ofSeconds(deepSeekProperties.getConnectTimeout()));
		}

		if (Objects.nonNull(deepSeekProperties.getReadTimeout())) {
			builder.readTimeout(Duration.ofSeconds(deepSeekProperties.getReadTimeout()));
		}

		if (Objects.nonNull(deepSeekProperties.getCallTimeout())) {
			builder.callTimeout(Duration.ofSeconds(deepSeekProperties.getCallTimeout()));
		}

		builder.logLevel(deepSeekProperties.getLogLevel());

		// 注入R1 提示词
		if (deepSeekProperties.isDefaultSystemPrompt()) {
			String systemMessage = ResourceUtil.getResourceAsString("prompts/system.pt");
			builder.systemMessage(systemMessage);
		}

		builder.searchApiKey(deepSeekProperties.getSearchApiKey());
		return builder.build();
	}

	@Bean
	@SneakyThrows
	@Condition(onMissingBean = EmbeddingClient.class, onProperty = "embedding.api-key")
	public EmbeddingClient embeddingClient(EmbeddingProperties embeddingProperties) {

		EmbeddingClient.Builder builder = EmbeddingClient.builder().baseUrl(embeddingProperties.getBaseUrl())
				.model(embeddingProperties.getModel()).openAiApiKey(embeddingProperties.getApiKey())
				.logRequests(embeddingProperties.isLogRequests()).logResponses(embeddingProperties.isLogResponses());

		if (Objects.nonNull(embeddingProperties.getProxy())) {
			builder.proxy(embeddingProperties.getProxy());
		}

		if (Objects.nonNull(embeddingProperties.getConnectTimeout())) {
			builder.connectTimeout(Duration.ofSeconds(embeddingProperties.getConnectTimeout()));
		}

		if (Objects.nonNull(embeddingProperties.getReadTimeout())) {
			builder.readTimeout(Duration.ofSeconds(embeddingProperties.getReadTimeout()));
		}

		if (Objects.nonNull(embeddingProperties.getCallTimeout())) {
			builder.callTimeout(Duration.ofSeconds(embeddingProperties.getCallTimeout()));
		}

		builder.logLevel(embeddingProperties.getLogLevel());

		return builder.build();
	}

}
