# DeepSeek4j Spring Boot Starter

详细的使用文档请访问：[https://javaai.pig4cloud.com/deepseek](https://javaai.pig4cloud.com/deepseek)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pig-mesh.ai/deepseek4j.svg?style=flat-square)](https://maven.badges.herokuapp.com/maven-central/io.github.pig-mesh.ai/deepseek4j)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

DeepSeek4j Spring Boot Starter 是一个用于快速集成 DeepSeek AI 能力的 Spring Boot 启动器。它提供了简单的配置方式和易用的 API，让你能够在 Spring Boot 项目中轻松使用 DeepSeek 的 AI 功能。

## 特性

- 完整的 DeepSeek API 支持，支持返回思维链和会话账单
- 支持自定义连接参数、代理配置、超时设置、请求响应日志
- Reactor 响应式支持，简化流式返回开发
  
## 快速开始

### Maven 依赖

在你的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.pig-mesh.ai</groupId>
    <artifactId>deepseek-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 基础配置

在 `application.yml` 或 `application.properties` 中添加必要的配置：

```yaml
deepseek:
  api-key: your-api-key-here
```

### 1. 快速入门

```java
@Autowired
private DeepSeekClient deepSeekClient;

// sse 流式返回
@GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ChatCompletionResponse> chat(String prompt) {
    return deepSeekClient.chatFluxCompletion(prompt);
}
```

### 2. 前端调试

双击运行根目录的 sse.html 文件，即可打开调试页面。在页面中输入后端 SSE 接口地址，点击发送后可实时查看推理过程和最终结果。页面提供了完整的前端实现代码，可作为集成参考。

<img src='https://minio.pigx.vip/oss/202502/1738864340.png' alt='1738864340'/>


## 许可证与致谢

本项目基于 [Apache License 2.0](LICENSE) 许可证开源。

项目设计灵感来源于 [OpenAI4J](https://github.com/ai-for-java/openai4j) 项目，在其优秀架构设计的基础上：
- 扩展了 DeepSeek 特有功能
- 增强了 Reactor 响应式支持
- 提供了更完整的 Spring Boot 集成

## 相关链接

- [PIG AI ](https://ai.pig4cloud.com)
- [DeepSeek 官方文档](https://platform.deepseek.com)
- [OpenAI4J 项目](https://github.com/ai-for-java/openai4j)

