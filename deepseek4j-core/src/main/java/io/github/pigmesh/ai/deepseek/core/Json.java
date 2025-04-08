package io.github.pigmesh.ai.deepseek.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;

public class Json {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.enable(SerializationFeature.INDENT_OUTPUT);

	public static String toJson(Object o) {
		try {
			return OBJECT_MAPPER.writeValueAsString(o);
		}
		catch (JsonProcessingException jpe) {
			throw new RuntimeException(jpe);
		}
	}

	public static <T> T fromJson(String json, Class<T> type) {
		try {
			// 针对 openrouter 的特殊处理
			if (json.contains("reasoning")) {
				json = json.replace("reasoning", "reasoning_content");
			}

			return OBJECT_MAPPER.readValue(json, type);
		}
		catch (JsonProcessingException jpe) {
			throw new RuntimeException(jpe);
		}
	}

	public static <T> T fromJson(String json, Class<T> type, Map<String, String> dynamicFieldNames) {
		try {
			return OBJECT_MAPPER.readValue(json, type);
		}
		catch (JsonProcessingException jpe) {
			throw new RuntimeException(jpe);
		}
	}

}
