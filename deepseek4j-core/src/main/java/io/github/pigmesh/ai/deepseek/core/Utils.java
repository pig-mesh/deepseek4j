package io.github.pigmesh.ai.deepseek.core;

import java.io.IOException;

class Utils {

    static RuntimeException toException(retrofit2.Response<?> response) throws IOException {
        return new OpenAiHttpException(response.code(), response.errorBody().string());
    }

    static RuntimeException toException(okhttp3.Response response) throws IOException {
        return new OpenAiHttpException(response.code(), response.body().string());
    }

    static <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
