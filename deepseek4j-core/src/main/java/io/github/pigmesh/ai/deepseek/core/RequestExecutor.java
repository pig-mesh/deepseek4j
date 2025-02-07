package io.github.pigmesh.ai.deepseek.core;

import okhttp3.OkHttpClient;
import retrofit2.Call;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class RequestExecutor<Request, Response, ResponseContent>
        implements SyncOrAsyncOrStreaming<ResponseContent> {

    private final Call<Response> call;
    private final Function<Response, ResponseContent> responseContentExtractor;

    private final OkHttpClient okHttpClient;
    private final String endpointUrl;
    private final Supplier<Request> requestWithStreamSupplier;
    private final Class<Response> responseClass;
    private final Function<Response, ResponseContent> streamEventContentExtractor;
    private final boolean logStreamingResponses;

    RequestExecutor(Call<Response> call,
                    Function<Response, ResponseContent> responseContentExtractor,

                    OkHttpClient okHttpClient,
                    String endpointUrl,
                    Supplier<Request> requestWithStreamSupplier,
                    Class<Response> responseClass,
                    Function<Response, ResponseContent> streamEventContentExtractor,
                    boolean logStreamingResponses
    ) {
        this.call = call;
        this.responseContentExtractor = responseContentExtractor;

        this.okHttpClient = okHttpClient;
        this.endpointUrl = endpointUrl;
        this.requestWithStreamSupplier = requestWithStreamSupplier;
        this.responseClass = responseClass;
        this.streamEventContentExtractor = streamEventContentExtractor;
        this.logStreamingResponses = logStreamingResponses;
    }

    RequestExecutor(Call<Response> call, Function<Response, ResponseContent> responseContentExtractor) {
        this.call = call;
        this.responseContentExtractor = responseContentExtractor;

        this.okHttpClient = null;
        this.endpointUrl = null;
        this.requestWithStreamSupplier = null;
        this.responseClass = null;
        this.streamEventContentExtractor = null;
        this.logStreamingResponses = false;
    }

    @Override
    public ResponseContent execute() {

        return new SyncRequestExecutor<>(call, responseContentExtractor).execute();
    }

    @Override
    public AsyncResponseHandling onResponse(Consumer<ResponseContent> responseHandler) {

        return new AsyncRequestExecutor<>(call, responseContentExtractor).onResponse(responseHandler);
    }

    @Override
    public StreamingResponseHandling onPartialResponse(Consumer<ResponseContent> partialResponseHandler) {

        return new StreamingRequestExecutor<>(
                okHttpClient,
                endpointUrl,
                requestWithStreamSupplier,
                responseClass,
                streamEventContentExtractor,
                logStreamingResponses
        ).onPartialResponse(partialResponseHandler);
    }
}
