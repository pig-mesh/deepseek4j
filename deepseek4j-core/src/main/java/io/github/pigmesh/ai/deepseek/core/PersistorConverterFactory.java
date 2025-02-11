package io.github.pigmesh.ai.deepseek.core;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Path;

class PersistorConverterFactory extends Converter.Factory {

	private final Path persistTo;

	PersistorConverterFactory(Path persistTo) {
		this.persistTo = persistTo;
	}

	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		return new PersistorConverter<>(retrofit.nextResponseBodyConverter(this, type, annotations));
	}

	private class PersistorConverter<T> implements Converter<ResponseBody, T> {

		private final Converter<ResponseBody, T> delegate;

		PersistorConverter(Converter<ResponseBody, T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T convert(ResponseBody value) throws IOException {
			T response = delegate.convert(value);

			return response;
		}

	}

}
