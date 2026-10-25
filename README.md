# deepseek4j （DeepSeek Java SDK）


<p align="center">
 <img src="https://gitcode.com/pig-mesh/deepseek4j/star/badge.svg"/>
</p>

详细的使用文档请访问：[https://javaai.pig4cloud.com/deepseek](https://javaai.pig4cloud.com/deepseek)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pig-mesh.ai/deepseek4j.svg?style=flat-square)](https://maven.badges.herokuapp.com/maven-central/io.github.pig-mesh.ai/deepseek4j)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

deepseek4j 是面向 DeepSeek 推出的 Java 开发 SDK，默认使用 DeepSeek Flash，也支持 DeepSeek V4 Pro。提供对话推理、函数调用、JSON结构化输出、以及基于 OpenAI 兼容 API 协议的嵌入向量生成能力。通过 Spring Boot Starter 模块，开发者可以快速为 Spring Boot 2.x/3.x 以及 Solon 等主流 Java Web 框架集成 AI 能力，提供开箱即用的配置体系、自动装配的客户端实例，以及便捷的流式响应支持。

## 特性

- 完整的 DeepSeek API 支持，支持返回思维链和会话账单
- 支持 DeepSeek 官方 Search 独立搜索
- 支持自定义连接参数、代理配置、超时设置、请求响应日志
- Reactor 响应式支持，简化流式返回开发
  
## 快速开始

### Maven 依赖

在你的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.pig-mesh.ai</groupId>
    <artifactId>deepseek-spring-boot-starter</artifactId>
    <version>1.6.0</version>
</dependency>
```

### 基础配置

在 `application.yml` 或 `application.properties` 中添加必要的配置：

```yaml
deepseek:
  api-key: your-api-key-here
```

默认模型为 `deepseek-flash`（`ChatCompletionModel.DEEPSEEK_FLASH`），使用 [DeepSeek 官方当前的 Flash 模型](https://api-docs.deepseek.com/zh-cn/quick_start/pricing/)。如需 Pro，可设置 `deepseek.model: deepseek-v4-pro`，或调用客户端 builder 的 `model("deepseek-v4-pro")`。单次请求指定的模型优先于客户端配置。

默认开启思考模式，思考强度为 `high`。旧枚举 `DEEPSEEK_V4_FLASH` 保留并标记为弃用，仍发送原模型名 `deepseek-v4-flash`；新调用建议使用 `DEEPSEEK_FLASH`。

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

### 3. DeepSeek 官方 Search

通过 `DeepSeekClient.search(...)` 调用 [DeepSeek 官方 Search API](https://api-docs.deepseek.com/zh-cn/api/search/)，获取结构化网页搜索结果。该接口使用现有 DeepSeek API Key，默认请求 `https://api.deepseek.com/v1/tools/search`，支持同步执行和异步回调。

`baseUrl` 只包含域名时（如 `https://api.deepseek.com`），Search 自动使用 `/v1/tools/search`。已配置路径时，在该路径下追加 `tools/search`，保留 `/v1/` 和自定义网关前缀；此处理不改变聊天接口的请求路径。

```java
import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchRequest;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchResponse;
import io.github.pigmesh.ai.deepseek.core.search.DeepSeekSearchResult;

DeepSeekClient client = DeepSeekClient.builder()
        .openAiApiKey("your-api-key-here")
        .build();
try {
    DeepSeekSearchRequest request = DeepSeekSearchRequest.builder()
            .query("DeepSeek Search API site:api-docs.deepseek.com")
            .count(5)
            .oriQuestion("如何调用 DeepSeek 官方搜索接口？")
            .localization("1.2.3.4")
            .needContent(true)
            .build();

    DeepSeekSearchResponse response = client.search(request).execute();
    for (DeepSeekSearchResult result : response.results()) {
        System.out.println(result.title() + " " + result.url());
        System.out.println(result.content());
    }
} finally {
    client.shutdown();
}
```

Spring Boot 中可直接使用已注入的 `DeepSeekClient`，无需新增配置。五个入参均支持通过 builder 设置：

| Java 参数 | JSON 字段 | 说明 |
| --- | --- | --- |
| `query` | `query` | 必填，1～128 个 Unicode 字符，不能全为空白；支持 `site:` 等查询语法 |
| `count` | `count` | 可选，1～50，服务端默认 10 |
| `oriQuestion` | `ori_question` | 可选，1～128 个字符；`null` 等同未提供 |
| `localization` | `localization` | 可选，用于优先返回本地结果的 IP 地址，服务端默认为空字符串 |
| `needContent` | `need_content` | 可选，服务端默认 `false`；为 `false` 时结果的 `content` 为空字符串 |

可选字段为 `null` 时不发送，显式设置的 `false` 会发送。查询原样透传，参数范围由服务端校验；错误沿用 `OpenAiHttpException`。无匹配结果时 `results()` 为空列表，结果中的 `siteName()`、`siteIcon()` 和 `date()` 可能为 `null`，其中日期保留为 UTC RFC 3339 字符串。

异步调用使用 `client.search(request).onResponse(responseHandler, errorHandler)`，客户端应在回调完成后关闭。搜索入口统一为 `client.search(request)`，直接使用现有 DeepSeek API Key 和基础地址，返回结构化搜索结果。


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

