package io.github.pigmesh.ai.deepseek.core.chat;

import lombok.Getter;

public enum ChatCompletionModel {

	/**
	 * 兼容旧模型名，新调用使用 {@link #DEEPSEEK_FLASH}。
	 */
	@Deprecated
	DEEPSEEK_V4_FLASH("deepseek-v4-flash"), //

	DEEPSEEK_V4_PRO("deepseek-v4-pro"), //

	@Deprecated
	DEEPSEEK_CHAT("deepseek-chat"), //

	@Deprecated
	DEEPSEEK_REASONER("deepseek-reasoner"), //

	/**
	 * DeepSeek 官方 Flash 模型名，跟随服务端更新。
	 */
	DEEPSEEK_FLASH("deepseek-flash");

	@Getter
	private final String value;

	ChatCompletionModel(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

}
