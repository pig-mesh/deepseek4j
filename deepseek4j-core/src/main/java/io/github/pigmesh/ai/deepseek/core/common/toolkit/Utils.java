package io.github.pigmesh.ai.deepseek.core.common.toolkit;

import io.github.pigmesh.ai.deepseek.core.common.exception.OpenAiHttpException;

import java.io.IOException;

public class Utils {

    public static RuntimeException toException(retrofit2.Response<?> response) throws IOException {
        return new OpenAiHttpException(response.code(), response.errorBody().string());
    }

    public static RuntimeException toException(okhttp3.Response response) throws IOException {
        return new OpenAiHttpException(response.code(), response.body().string());
    }

    public static <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
