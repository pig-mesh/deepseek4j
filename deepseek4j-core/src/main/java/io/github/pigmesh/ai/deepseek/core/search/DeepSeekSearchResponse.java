package io.github.pigmesh.ai.deepseek.core.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DeepSeek 官方 Search API 响应，无匹配结果时 results 为空列表。
 */
@JsonDeserialize(builder = DeepSeekSearchResponse.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class DeepSeekSearchResponse {

	@JsonProperty
	private final List<DeepSeekSearchResult> results;

	private DeepSeekSearchResponse(Builder builder) {
		this.results = builder.results;
	}

	public List<DeepSeekSearchResult> results() {
		return results;
	}

	@Override
	public boolean equals(Object another) {
		if (this == another)
			return true;
		if (!(another instanceof DeepSeekSearchResponse))
			return false;
		DeepSeekSearchResponse that = (DeepSeekSearchResponse) another;
		return Objects.equals(results, that.results);
	}

	@Override
	public int hashCode() {
		return Objects.hash(results);
	}

	@Override
	public String toString() {
		return "DeepSeekSearchResponse{" + "results=" + results + "}";
	}

	public static Builder builder() {
		return new Builder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static final class Builder {

		private List<DeepSeekSearchResult> results;

		private Builder() {
		}

		public Builder results(List<DeepSeekSearchResult> results) {
			this.results = results == null ? null : Collections.unmodifiableList(new ArrayList<>(results));
			return this;
		}

		public DeepSeekSearchResponse build() {
			return new DeepSeekSearchResponse(this);
		}

	}

}
