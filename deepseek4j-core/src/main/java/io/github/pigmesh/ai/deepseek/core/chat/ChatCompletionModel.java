package io.github.pigmesh.ai.deepseek.core.chat;

import lombok.Getter;

public enum ChatCompletionModel {

	DEEPSEEK_V4_FLASH("deepseek-v4-flash"), //

	DEEPSEEK_V4_PRO("deepseek-v4-pro"), //

	@Deprecated
	DEEPSEEK_CHAT("deepseek-chat"), //

	@Deprecated
	DEEPSEEK_REASONER("deepseek-reasoner");

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
