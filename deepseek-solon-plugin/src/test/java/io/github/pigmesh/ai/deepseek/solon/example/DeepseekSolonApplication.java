package io.github.pigmesh.ai.deepseek.solon.example;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.web.cors.CrossFilter;

/**
 * @author songyinyin
 * @since 2025/2/16 15:27
 */
@SolonMain
public class DeepseekSolonApplication {

	public static void main(String[] args) {
		Solon.start(DeepseekSolonApplication.class, args, app -> {
			// 例：或者：增加全局处理（用过滤器模式）
			app.filter(-1, new CrossFilter().allowedOrigins("*")); // 加-1 优先级更高
		});
	}

}
