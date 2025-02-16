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
@BindProps(prefix = "deepseek")
@Configuration
public class DeepSeekProperties extends DeepSeekConfig {

}
