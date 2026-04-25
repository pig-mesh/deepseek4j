package io.github.pigmesh.ai.deepseek.core.chat;

import lombok.Getter;

/**
 * DeepSeek 思考模式开关。
 */
public enum ThinkingType {

	ENABLED("enabled"),

	DISABLED("disabled");

	@Getter
	private final String value;

	ThinkingType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

}
