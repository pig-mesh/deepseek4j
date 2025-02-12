package io.github.pigmesh.ai.deepseek.example.embed;

import io.github.pigmesh.ai.deepseek.core.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * @author lengleng
 * @date 2025/2/11
 */
@RestController
@RequestMapping("/embed")
@RequiredArgsConstructor
public class EmbedController {

	private final Optional<EmbeddingClient> embeddingClientOptional;

	@GetMapping("/get")
	public List<Float> getEmbed() {
		return embeddingClientOptional.get().embed("Hello, world!");
	}

}
