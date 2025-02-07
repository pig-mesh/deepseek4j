package io.github.pigmesh.ai.deepseek.core.config;

import io.github.pigmesh.ai.deepseek.core.common.enums.LogLevel;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.github.pigmesh.ai.deepseek.core.config.RequestLoggingInterceptor.inOneLine;

public class ResponseLoggingInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);

    private LogLevel logLevel = LogLevel.DEBUG;

    public ResponseLoggingInterceptor() {
    }

    public ResponseLoggingInterceptor(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        log(response);

        return response;
    }

    public void log(Response response) {
        String message = "Response:\n- status code: {}\n- headers: {}\n- body: {}";

        try {
            switch (logLevel) {
                case INFO:
                    logInfo(response, message);
                    break;
                case WARN:
                    logWarn(response, message);
                    break;
                case ERROR:
                    logError(response, message);
                    break;
                default:
                    logDebug(response, message);
            }

        } catch (IOException e) {
            log.warn("Failed to log response", e);
        }
    }

    private void logError(Response response, String message) throws IOException {
        log.error(
                message,
                response.code(),
                inOneLine(response.headers()),
                getBody(response)
        );
    }

    private void logWarn(Response response, String message) throws IOException {
        log.warn(
                message,
                response.code(),
                inOneLine(response.headers()),
                getBody(response)
        );
    }

    private void logInfo(Response response, String message) throws IOException {
        log.info(
                message,
                response.code(),
                inOneLine(response.headers()),
                getBody(response)
        );
    }

    private void logDebug(Response response, String message) throws IOException {
        log.debug(
                message,
                response.code(),
                inOneLine(response.headers()),
                getBody(response)
        );
    }

    private String getBody(Response response) throws IOException {
        if (isEventStream(response)) {
            return "[skipping response body due to streaming]";
        } else {
            return response.peekBody(Long.MAX_VALUE).string();
        }
    }

    private boolean isEventStream(Response response) {
        String contentType = response.header("content-type");
        return contentType != null && contentType.contains("event-stream");
    }
}
