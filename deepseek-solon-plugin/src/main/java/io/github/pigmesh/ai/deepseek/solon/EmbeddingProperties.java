package io.github.pigmesh.ai.deepseek.solon;

import io.github.pigmesh.ai.deepseek.core.DeepSeekConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;

/**
 * @author songyinyin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@BindProps(prefix = "embedding")
@Configuration
public class EmbeddingProperties extends DeepSeekConfig {

	/**
	 * 基本 URL
	 */
	private String baseUrl = "http://127.0.0.1:11434/v1";

}
