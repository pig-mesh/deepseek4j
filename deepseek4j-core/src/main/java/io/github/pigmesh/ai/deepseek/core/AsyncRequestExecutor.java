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

	void onResponse(Consumer<ResponseContent> responseHandler, Consumer<Throwable> errorHandler) {
		call.enqueue(new retrofit2.Callback<Response>() {
			@Override
			public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
				if (response.isSuccessful()) {
					responseHandler.accept(responseContentExtractor.apply(response.body()));
				}
				else {
					try {
						errorHandler.accept(toException(response));
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}

			@Override
			public void onFailure(Call<Response> call, Throwable t) {
				errorHandler.accept(t);
			}
		});
	}

}
