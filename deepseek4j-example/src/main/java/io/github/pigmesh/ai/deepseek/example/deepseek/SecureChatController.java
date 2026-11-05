package io.github.pigmesh.ai.deepseek.example.deepseek;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 受 Spring Security 保护的流式接口示例：/secure/** 需要认证后才能访问。
 *
 * <p>
 * 认证与授权发生在过滤器链中、流开始输出之前，保证安全异常不会出现在响应已提交之后。
 */
@RestController
@RequestMapping("/secure")
@RequiredArgsConstructor
public class SecureChatController {

	private final DeepSeekClient deepSeekClient;

	@GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE + "; charset=UTF-8")
	public Flux<ChatCompletionResponse> chat(String prompt) {
		return deepSeekClient.chatFluxCompletion(prompt);
	}

}
