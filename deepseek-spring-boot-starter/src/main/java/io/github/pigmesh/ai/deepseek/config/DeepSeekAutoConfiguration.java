package io.github.pigmesh.ai.deepseek.config;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.OpenAiClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
@EnableConfigurationProperties(DeepSeekProperties.class)
public class DeepSeekAutoConfiguration {

    /**
     * Deep Seek 客户端
     *
     * @param deepSeekProperties Deep Seek 属性
     * @return {@link OpenAiClient }
     */
    @Bean
    @ConditionalOnMissingBean
    public DeepSeekClient deepSeekClient(DeepSeekProperties deepSeekProperties) {

        DeepSeekClient.Builder builder = DeepSeekClient.builder()
                .baseUrl(deepSeekProperties.getBaseUrl())
                .openAiApiKey(deepSeekProperties.getApiKey())
                .logRequests(deepSeekProperties.isLogRequests())
                .logResponses(deepSeekProperties.isLogResponses());

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
        return builder.build();
    }
}
