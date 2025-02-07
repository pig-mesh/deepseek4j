package io.github.pigmesh.ai.deepseek.core.chat;

import io.github.pigmesh.ai.deepseek.core.common.toolkit.Json;

import java.util.Map;

public class FunctionCallUtil {

    public static <T> T argument(String name, FunctionCall function) {
        Map<String, Object> arguments = argumentsAsMap(function.arguments()); // TODO cache?
        return (T) arguments.get(name);
    }

    public static Map<String, Object> argumentsAsMap(String arguments) {
        return Json.fromJson(arguments, Map.class);
    }
}
