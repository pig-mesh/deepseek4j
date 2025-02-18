package io.github.pigmesh.ai.deepseek.core.chat;

import lombok.Getter;

public enum ChatCompletionModel {

	DEEPSEEK_CHAT("deepseek-chat"), //
	// alias
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
