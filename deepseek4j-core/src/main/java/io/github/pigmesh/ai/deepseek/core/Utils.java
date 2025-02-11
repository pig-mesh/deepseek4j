package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.search.SearchResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Utils {

	static RuntimeException toException(retrofit2.Response<?> response) throws IOException {
		return new OpenAiHttpException(response.code(), response.errorBody().string());
	}

	static RuntimeException toException(okhttp3.Response response) throws IOException {
		return new OpenAiHttpException(response.code(), response.body().string());
	}

	static <T> T getOrDefault(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	public static String format(String user, SearchResponse.Value[] values) {
		if (Objects.isNull(values) || values.length == 0) {
			return null;
		}

		String results = Arrays
				.stream(values).map(
						organicResult -> "Title: " + organicResult.getName() + "\n" + "Source: "
								+ organicResult.getUrl() + "\n"
								+ (organicResult.getSummary() != null ? "Content:" + "\n" + organicResult.getSummary()
										: "Snippet:" + "\n" + organicResult.getSnippet()))
				.collect(Collectors.joining("\n\n"));
		return String.format("用户输入提问: %s\n\n 当前时间:%s \n\n 参考如下内容进行推理回答:%s", user,
				LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), results);
	}

}
