package io.github.pigmesh.ai.deepseek.core;

import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionRequest;
import io.github.pigmesh.ai.deepseek.core.completion.CompletionResponse;
import io.github.pigmesh.ai.deepseek.core.models.ModelsResponse;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationRequest;
import io.github.pigmesh.ai.deepseek.core.moderation.ModerationResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

interface OpenAiApi {

	@POST("completions")
	@Headers("Content-Type: application/json")
	Call<CompletionResponse> completions(@Body CompletionRequest request, @Query("api-version") String apiVersion);

	@POST("completions")
	@Headers("Content-Type: application/json")
	Call<CompletionResponse> completions(@HeaderMap Map<String, String> headers, @Body CompletionRequest request,
			@Query("api-version") String apiVersion);

	@POST("chat/completions")
	@Headers("Content-Type: application/json")
	Call<ChatCompletionResponse> chatCompletions(@Body ChatCompletionRequest request,
			@Query("api-version") String apiVersion);

	@POST("chat/completions")
	@Headers("Content-Type: application/json")
	Call<ChatCompletionResponse> chatCompletions(@HeaderMap Map<String, String> headers,
			@Body ChatCompletionRequest request, @Query("api-version") String apiVersion);

	@POST("moderations")
	@Headers("Content-Type: application/json")
	Call<ModerationResponse> moderations(@Body ModerationRequest request, @Query("api-version") String apiVersion);

	@POST("moderations")
	@Headers("Content-Type: application/json")
	Call<ModerationResponse> moderations(@HeaderMap Map<String, String> headers, @Body ModerationRequest request,
			@Query("api-version") String apiVersion);

	@GET("models")
	@Headers("Content-Type: application/json")
	Call<ModelsResponse> models(@HeaderMap Map<String, String> headers, @Query("api-version") String apiVersion);

}
