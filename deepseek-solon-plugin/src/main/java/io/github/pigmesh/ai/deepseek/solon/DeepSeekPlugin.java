package io.github.pigmesh.ai.deepseek.solon;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * solon deepseek4j plugin
 *
 * @author songyinyin
 * @since 2025/2/16 13:03
 */
public class DeepSeekPlugin implements Plugin {

	@Override
	public void start(AppContext context) throws Throwable {
		// 扫描当前包
		context.beanScan(DeepSeekPlugin.class);
	}

}
