# deepseek4j （DeepSeek Java SDK）

详细的使用文档请访问：[https://javaai.pig4cloud.com/deepseek](https://javaai.pig4cloud.com/deepseek)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pig-mesh.ai/deepseek4j.svg?style=flat-square)](https://maven.badges.herokuapp.com/maven-central/io.github.pig-mesh.ai/deepseek4j)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

deepseek4j 是面向 DeepSeek 推出的 Java 开发 SDK，支持 DeepSeek R1 和 V3 全系列模型。提供对话推理、函数调用、JSON结构化输出、以及基于 OpenAI 兼容 API 协议的嵌入向量生成能力。通过 Spring Boot Starter 模块，开发者可以快速为 Spring Boot 2.x/3.x 以及 Solon 等主流 Java Web 框架集成 AI 能力，提供开箱即用的配置体系、自动装配的客户端实例，以及便捷的流式响应支持。

## 特性

- 完整的 DeepSeek API 支持，支持返回思维链和会话账单
- 支持 WebSearch 联网搜索
- 支持自定义连接参数、代理配置、超时设置、请求响应日志
- Reactor 响应式支持，简化流式返回开发
  
## 快速开始

### Maven 依赖

在你的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.pig-mesh.ai</groupId>
    <artifactId>deepseek-spring-boot-starter</artifactId>
    <version>1.4.7</version>
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

项目设计灵感来源于 [OpenAI4J](https://github.com/ai-for-java/openai4j) 项目，由于其不再维护，在其优秀架构设计的基础上：
- 扩展了 DeepSeek 特有功能
- 增强了 Reactor 响应式支持
- 提供了更完整的 Spring Boot 集成

## 相关链接

- [PIG AI ](https://ai.pig4cloud.com)
- [DeepSeek 官方文档](https://platform.deepseek.com)
- [OpenAI4J 项目](https://github.com/ai-for-java/openai4j)

