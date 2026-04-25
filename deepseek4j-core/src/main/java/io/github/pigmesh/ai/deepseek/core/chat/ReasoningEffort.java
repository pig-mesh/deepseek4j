package io.github.pigmesh.ai.deepseek.core.chat;

import lombok.Getter;

/**
 * DeepSeek 思考强度。
 */
public enum ReasoningEffort {

	LOW("high"),

	MEDIUM("high"),

	HIGH("high"),

	XHIGH("max"),

	MAX("max");

	@Getter
	private final String value;

	ReasoningEffort(String value) {
		this.value = value;
	}

	/**
	 * 将兼容值转换为 DeepSeek V4 支持的思考强度。
	 * @param value 调用方传入的思考强度
	 * @return 可发送到 DeepSeek API 的思考强度
	 */
	public static String normalize(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim().toLowerCase();
		if ("low".equals(normalized) || "medium".equals(normalized)) {
			return HIGH.value;
		}
		if ("xhigh".equals(normalized)) {
			return MAX.value;
		}
		return normalized;
	}

	@Override
	public String toString() {
		return value;
	}

}
