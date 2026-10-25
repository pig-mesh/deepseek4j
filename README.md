# deepseek4j （DeepSeek Java SDK）


<p align="center">
 <img src="https://gitcode.com/pig-mesh/deepseek4j/star/badge.svg"/>
</p>

详细的使用文档请访问：[https://javaai.pig4cloud.com/deepseek](https://javaai.pig4cloud.com/deepseek)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.pig-mesh.ai/deepseek4j.svg?style=flat-square)](https://maven.badges.herokuapp.com/maven-central/io.github.pig-mesh.ai/deepseek4j)
[![GitHub Release](https://img.shields.io/github/v/release/pig-mesh/deepseek4j?style=flat-square)](https://github.com/pig-mesh/deepseek4j/releases/latest)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

deepseek4j 是面向 DeepSeek 推出的 Java 开发 SDK，默认使用 DeepSeek Flash，也支持 DeepSeek V4 Pro。提供对话推理、函数调用、JSON结构化输出、以及基于 OpenAI 兼容 API 协议的嵌入向量生成能力。通过 Spring Boot Starter 模块，开发者可以快速为 Spring Boot 2.x/3.x 以及 Solon 等主流 Java Web 框架集成 AI 能力，提供开箱即用的配置体系、自动装配的客户端实例，以及便捷的流式响应支持。

## 1.6.1 更新

`1.6.1` 已发布到 Maven Central，完整发布说明见 [GitHub Release v1.6.1](https://github.com/pig-mesh/deepseek4j/releases/tag/v1.6.1)。

- 新增官方搜索与模型回答的组合调用 `chatSearchCompletion(...)`，自动搜索、整理参考资料并流式生成回答。
- 组合调用复用客户端配置，保留历史消息和单次请求参数；取消订阅会停止当前 HTTP 请求。
- 保留 `1.6.0` 的官方独立搜索接口和默认模型 `deepseek-flash`，无需额外的搜索 API Key。

## 特性

- 完整的 DeepSeek API 支持，支持返回思维链和会话账单
- 支持 DeepSeek 官方 Search 独立搜索，以及搜索后由模型生成回答的组合调用
- 支持自定义连接参数、代理配置、超时设置、请求响应日志
- Reactor 响应式支持，简化流式返回开发
  
## 快速开始

运行环境要求 JDK 8 或更高版本。

### Maven 依赖

Spring Boot 项目在 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.pig-mesh.ai</groupId>
    <artifactId>deepseek-spring-boot-starter</artifactId>
    <version>1.6.1</version>
</dependency>
```

不使用 Spring Boot 时，可直接依赖核心 SDK，并通过 `DeepSeekClient.builder()` 创建客户端：

```xml
<dependency>
    <groupId>io.github.pig-mesh.ai</groupId>
    <artifactId>deepseek4j-core</artifactId>
    <version>1.6.1</version>
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

异步调用使用 `client.search(request).onResponse(responseHandler, errorHandler)`，客户端应在回调完成后关闭。独立搜索入口为 `client.search(request)`，直接使用现有 DeepSeek API Key 和基础地址，返回结构化搜索结果。

### 4. 搜索后由模型生成回答

`1.6.1` 提供 `chatSearchCompletion(...)`，在 SDK 内串联官方 Search 和流式聊天。Spring Boot 中直接使用注入的 `DeepSeekClient`，无需手动拼装搜索结果或新增搜索配置：

```java
@GetMapping(value = "/chat/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ChatCompletionResponse> chatSearch(String prompt) {
    return deepSeekClient.chatSearchCompletion(prompt);
}
```

简单调用默认搜索 5 条结果，设置 `needContent(false)`，使用标题、来源 URL 和摘要作为参考资料。问题同时作为搜索 `query`，须为 1～128 个 Unicode 字符且不能全为空白。长问题使用独立的短查询，SDK 会保留发给模型的完整问题：

```java
DeepSeekSearchRequest search = DeepSeekSearchRequest.builder()
        .query("DeepSeek Search API site:api-docs.deepseek.com")
        .count(5)
        .needContent(true)
        .build();
Flux<ChatCompletionResponse> answer = deepSeekClient.chatSearchCompletion(
        "请结合官方文档，说明搜索接口如何与多轮对话、流式输出和错误处理配合使用。", search);
```

需要保留历史消息或覆盖模型参数时，传入完整对话请求：

```java
ChatCompletionRequest chat = ChatCompletionRequest.builder()
        .model("deepseek-v4-pro")
        .thinkingDisabled()
        .addSystemMessage("请用中文回答，并标明引用来源。")
        .addUserMessage("我正在接入 DeepSeek。")
        .addAssistantMessage("你需要了解哪部分？")
        .addUserMessage("如何接入官方搜索？")
        .build();
Flux<ChatCompletionResponse> answer = deepSeekClient.chatSearchCompletion(chat, search);
```

- 搜索和聊天复用同一个客户端的 API Key、基础地址、代理、超时与自定义 headers。模型和思考参数以单次请求为准，未设置时继承客户端配置；请求没有 system 消息时补充客户端 `systemMessage`。
- 历史消息和原问题保持不变。SDK 在最后一条 user 消息之前插入一条参考资料消息，将网页内容标记为外部资料，并提示模型标注来源 URL。
- `needContent(true)` 时优先使用正文，没有正文时回退到摘要，再回退到 snippet；默认使用摘要。每条资料最多保留 3,000 个正文字符，全部参考资料不超过 20,000 个 UTF-16 代码单元，避免大网页无限增加上下文。
- 返回的是冷 `Flux`：订阅后才发起搜索，每次订阅独立执行。搜索成功后开始模型流式请求；取消订阅会取消当前搜索或聊天请求。
- 搜索无结果时使用原问题继续聊天。搜索 HTTP、网络或解析错误通过 `onError` 返回，不会静默调用模型；模型错误同样向调用方传递。

## 从 1.5.0 升级

`1.6.0` 包含搜索相关的破坏性 API 变更：

1. 将 starter 或 core 依赖版本统一更新为 `1.6.1`。
2. 删除 Bocha 专用的 `deepseek.search-api-key` 配置，以及客户端 builder 的 `searchApi(...)`、`searchApiKey(...)`、`searchEndpoint(...)` 调用。官方 Search 复用 DeepSeek API Key 和基础地址。
3. 需要搜索后生成回答时，使用 `1.6.1` 的 `chatSearchCompletion(...)`，返回 `Flux<ChatCompletionResponse>`；旧 `chatSearchStreamingCompletion(...)` 调用也迁移到该方法。仅需结构化搜索结果时，使用 `client.search(DeepSeekSearchRequest)`。
4. 将旧的 `SearchRequest`、`SearchResponse` 替换为 `DeepSeekSearchRequest`、`DeepSeekSearchResponse`，并按上文官方 Search 的参数和结果结构调整代码。Bocha 专用的 `SearchApi` 和 `FreshnessEnums` 已移除。


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

