package io.github.pigmesh.ai.deepseek.config;

import io.github.pigmesh.ai.deepseek.core.common.enums.LogLevel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.Proxy;

import static io.github.pigmesh.ai.deepseek.core.common.enums.LogLevel.DEBUG;


/**
 * @author lengleng
 * @date 2025/2/6
 */
@Data
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    /**
     * 基本 URL
     */
    private String baseUrl = "https://api.deepseek.com/v1";
    /**
     * API 密钥
     */
    private String apiKey;
    /**
     * 日志请求
     */
    private boolean logRequests;
    /**
     * 日志响应
     */
    private boolean logResponses;

    /**
     * 代理
     */
    private Proxy proxy;

    /**
     * 连接超时 S
     */
    private Integer connectTimeout;

    /**
     * 读取超时 S
     */
    private Integer readTimeout;

    /**
     * 呼叫超时 S
     */
    private Integer callTimeout;


    /**
     * 日志级别
     */
    public LogLevel logLevel = DEBUG;

}
