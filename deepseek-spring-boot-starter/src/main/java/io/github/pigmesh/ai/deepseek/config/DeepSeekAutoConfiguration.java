package io.github.pigmesh.ai.deepseek.config;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.EmbeddingClient;
import io.github.pigmesh.ai.deepseek.core.OpenAiClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * Deep Seek 自动配置
 *
 * @author lengleng
 * @date 2025/02/06
 */
@AutoConfiguration
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ DeepSeekProperties.class, EmbeddingProperties.class })
public class DeepSeekAutoConfiguration {

	@Value("classpath:/prompts/system.pt")
	private Resource systemResource;

	/**
	 * Deep Seek 客户端
	 * @param deepSeekProperties Deep Seek 属性
	 * @return {@link OpenAiClient }
	 */
	@SneakyThrows
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "deepseek", name = "api-key")
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
			String systemMessage = StreamUtils.copyToString(systemResource.getInputStream(), StandardCharsets.UTF_8);
			builder.systemMessage(systemMessage);
		}

		builder.searchApiKey(deepSeekProperties.getSearchApiKey());
		return builder.build();
	}

	/**
	 * Embedding 客户端
	 * @param embeddingProperties Embedding 属性
	 * @return {@link OpenAiClient }
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "embedding", name = "api-key")
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
