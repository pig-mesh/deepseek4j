package io.github.pigmesh.ai.deepseek.solon.example.controller;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.Json;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionChoice;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.models.ModelsResponse;
import io.github.pigmesh.ai.deepseek.solon.DeepSeekProperties;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Param;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author songyinyin
 * @since 2025/2/16 15:37
 */
@Slf4j
@Controller
public class OllamaLocalController {

	@Inject
	private DeepSeekClient deepSeekClient;

	@Inject
	private DeepSeekProperties deepSeekProperties;

	@Get
	@Mapping(value = "/ollama/chat", produces = "text/event-stream; charset=UTF-8")
	public Flux<ChatCompletionResponse> chat(@Param("prompt") String prompt) {
		return deepSeekClient.chatFluxCompletion(prompt);
	}

	Function<List<ChatCompletionChoice>, String> choicesProcess = list -> list.stream().map(e -> e.delta().content())
			.collect(Collectors.joining());

	@Get
	@Mapping(value = "/ollama/models", produces = "application/json")
	public ModelsResponse models() {
		return deepSeekClient.models();
	}

	public final static HashMap<String, String> cache = new HashMap<>();

	// 同步
	@Get
	@Mapping(value = "/ollama/sync/chat")
	public ChatCompletionResponse syncChat(@Param("prompt") String prompt) {
		ChatCompletionRequest request = ChatCompletionRequest.builder()
				// 根据渠道模型名称动态修改这个参数
				.model(deepSeekProperties.getModel()).addUserMessage(prompt).build();

		return deepSeekClient.chatCompletion(request).execute();
	}

	@Get
	@Mapping(value = "/ollama/chat/advanced", produces = "text/event-stream")
	public Flux<ChatCompletionResponse> chatAdvanced(@Param("prompt") String prompt,
			@Param("cacheCode") String cacheCode) {
		log.info("cacheCode {}", cacheCode);

		ChatCompletionRequest request = ChatCompletionRequest.builder().model(deepSeekProperties.getModel())
				.addUserMessage(prompt).addAssistantMessage(elt.apply(cache.getOrDefault(cacheCode, "")))
				.addSystemMessage("你是一个专业的助手").maxCompletionTokens(5000).build();
		log.info("request {}", Json.toJson(request));
		// 只保留上一次回答内容
		cache.remove(cacheCode);
		return deepSeekClient.chatFluxCompletion(request).doOnNext(i -> {
			String content = choicesProcess.apply(i.choices());
			// 其他ELT流程
			cache.merge(cacheCode, content, String::concat);
		}).doOnError(e -> log.error("/chat/advanced error:{}", e.getMessage()));
	}

	Function<String, String> elt = s -> s.replaceAll("<think>[\\s\\S]*?</think>", "").replaceAll("\n", "");

}
