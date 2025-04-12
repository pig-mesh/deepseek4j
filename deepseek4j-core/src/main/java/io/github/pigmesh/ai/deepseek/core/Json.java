package io.github.pigmesh.ai.deepseek.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;

import java.util.Map;

public class Json {

	public static final ObjectMapper OBJECT_MAPPER;

	static {
		OBJECT_MAPPER = (new ObjectMapper()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.enable(SerializationFeature.INDENT_OUTPUT);
		OBJECT_MAPPER.coercionConfigFor(LogicalType.Enum).setCoercion(CoercionInputShape.EmptyString,
				CoercionAction.AsNull);
	}

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
