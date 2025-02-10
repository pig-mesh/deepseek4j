package io.github.pigmesh.ai.deepseek.example.deepseek;

import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.models.ModelsResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

@RestController
@RequestMapping("/deepseek")
public class DeepSeekLocalController {

    @Resource
    private DeepSeekClient deepSeekClient;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatCompletionResponse> chat(String prompt) {
        return deepSeekClient.chatFluxCompletion(prompt);
    }

    @GetMapping(value = "/models", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelsResponse models() {
        return deepSeekClient.models();
    }

}
