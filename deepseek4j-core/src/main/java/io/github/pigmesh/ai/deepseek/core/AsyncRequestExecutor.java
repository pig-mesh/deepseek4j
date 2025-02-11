package io.github.pigmesh.ai.deepseek.core;

import retrofit2.Call;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.github.pigmesh.ai.deepseek.core.Utils.toException;

class AsyncRequestExecutor<Response, ResponseContent> {

	private final Call<Response> call;

	private final Function<Response, ResponseContent> responseContentExtractor;

	AsyncRequestExecutor(Call<Response> call, Function<Response, ResponseContent> responseContentExtractor) {
		this.call = call;
		this.responseContentExtractor = responseContentExtractor;
	}

	AsyncResponseHandling onResponse(Consumer<ResponseContent> responseHandler) {
		return new AsyncResponseHandling() {

			@Override
			public ErrorHandling onError(Consumer<Throwable> errorHandler) {
				return new ErrorHandling() {

					@Override
					public ResponseHandle execute() {
						try {
							retrofit2.Response<Response> retrofitResponse = call.execute();
							if (retrofitResponse.isSuccessful()) {
								Response response = retrofitResponse.body();
								ResponseContent responseContent = responseContentExtractor.apply(response);
								responseHandler.accept(responseContent);
							}
							else {
								errorHandler.accept(toException(retrofitResponse));
							}
						}
						catch (IOException e) {
							errorHandler.accept(e);
						}
						return new ResponseHandle();
					}
				};
			}

			@Override
			public ErrorHandling ignoreErrors() {
				return new ErrorHandling() {

					@Override
					public ResponseHandle execute() {
						try {
							retrofit2.Response<Response> retrofitResponse = call.execute();
							if (retrofitResponse.isSuccessful()) {
								Response response = retrofitResponse.body();
								ResponseContent responseContent = responseContentExtractor.apply(response);
								responseHandler.accept(responseContent);
							}
						}
						catch (IOException e) {
							// intentionally ignoring, because user called ignoreErrors()
						}
						return new ResponseHandle();
					}
				};
			}
		};
	}

}
