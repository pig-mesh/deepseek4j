package io.github.pigmesh.ai.deepseek.core;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

class RequestLoggingInterceptor implements Interceptor {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

	private static final Set<String> COMMON_SECRET_HEADERS = new HashSet<>(
			asList("authorization", "x-api-key", "x-auth-token"));

	private static final String BEARER = "Bearer";

	private LogLevel logLevel = LogLevel.DEBUG;

	public RequestLoggingInterceptor() {
	}

	public RequestLoggingInterceptor(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();

		log(request);

		return chain.proceed(request);
	}

	private void log(Request request) {
		String message = "Request:\n- method: {}\n- url: {}\n- headers: {}\n- body: {}";

		try {
			switch (logLevel) {
			case INFO:
				logInfo(request, message);
				break;
			case WARN:
				logWarn(request, message);
				break;
			case ERROR:
				logError(request, message);
				break;
			default:
				logDebug(request, message);
			}
		}
		catch (Exception e) {
			log.warn("Failed to log request", e);
		}
	}

	private void logInfo(Request request, String message) {
		log.info(message, request.method(), request.url(), inOneLine(request.headers()), getBody(request));
	}

	private void logWarn(Request request, String message) {
		log.warn(message, request.method(), request.url(), inOneLine(request.headers()), getBody(request));
	}

	private void logError(Request request, String message) {
		log.error(message, request.method(), request.url(), inOneLine(request.headers()), getBody(request));
	}

	private void logDebug(Request request, String message) {
		log.debug(message, request.method(), request.url(), inOneLine(request.headers()), getBody(request));
	}

	static String inOneLine(Headers headers) {

		return stream(headers.spliterator(), false).map(header -> format(header.component1(), header.component2()))
				.collect(joining(", "));
	}

	static String format(String headerKey, String headerValue) {
		if (COMMON_SECRET_HEADERS.contains(headerKey.toLowerCase())) {
			headerValue = maskSecretKey(headerValue);
		}
		return String.format("[%s: %s]", headerKey, headerValue);
	}

	static String maskSecretKey(String key) {
		if (key == null || key.trim().isEmpty()) {
			return key;
		}

		try {
			if (key.startsWith(BEARER)) {
				return BEARER + " " + mask(key.substring(BEARER.length() + 1));
			}
			else {
				return mask(key);
			}
		}
		catch (Exception e) {
			return "Failed to mask the API key.";
		}
	}

	private static String mask(String key) {
		if (key.length() >= 7) {
			return key.substring(0, 5) + "..." + key.substring(key.length() - 2);
		}
		else {
			return "...";
		}
	}

	private static String getBody(Request request) {
		try {
			Buffer buffer = new Buffer();
			if (request.body() != null) {
				request.body().writeTo(buffer);
			}
			return buffer.readUtf8();
		}
		catch (Exception e) {
			log.warn("Exception happened while reading request body", e);
			return "[Exception happened while reading request body. Check logs for more details.]";
		}
	}

}
