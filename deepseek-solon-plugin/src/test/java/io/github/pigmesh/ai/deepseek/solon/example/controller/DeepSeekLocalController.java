package io.github.pigmesh.ai.deepseek.solon.example.controller;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.models.ModelsResponse;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Param;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

/**
 * @author songyinyin
 * @since 2025/2/16 15:35
 */
@Controller
public class DeepSeekLocalController {

	@Inject
	private DeepSeekClient deepSeekClient;

	@Get
	@Mapping(value = "/chat")
	public String chat(@Param("prompt") String prompt) {
		return deepSeekClient.chatCompletion(prompt).execute();
	}

	@Get
	@Mapping(value = "/models", produces = "application/json")
	public ModelsResponse models() {
		return deepSeekClient.models();
	}

}
