package io.github.pigmesh.ai.deepseek.core.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import okhttp3.Interceptor;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class GenericHeaderInjector implements Interceptor {
    private final Map<String, String> headers = new HashMap<>();

    public GenericHeaderInjector(Map<String, String> headers) {
        Optional.ofNullable(headers)
            .ifPresent(this.headers::putAll);
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Builder builder = chain.request().newBuilder();

        // Add headers
        this.headers.forEach(builder::addHeader);

        return chain.proceed(builder.build());
    }
}
