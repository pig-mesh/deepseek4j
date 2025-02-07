package io.github.pigmesh.ai.deepseek.core.common;

import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Path;

public class PersistorConverterFactory extends Converter.Factory {

    private final Path persistTo;

    public PersistorConverterFactory(Path persistTo) {
        this.persistTo = persistTo;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(@NotNull Type type, @NotNull Annotation[] annotations, Retrofit retrofit) {
        return new PersistorConverter<>(retrofit.nextResponseBodyConverter(this, type, annotations));
    }

    private static class PersistorConverter<T> implements Converter<ResponseBody, T> {

        private final Converter<ResponseBody, T> delegate;

        PersistorConverter(Converter<ResponseBody, T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T convert(@NotNull ResponseBody value) throws IOException {
            return delegate.convert(value);
        }
    }
}
