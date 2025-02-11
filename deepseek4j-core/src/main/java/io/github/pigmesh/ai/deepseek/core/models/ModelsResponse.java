package io.github.pigmesh.ai.deepseek.core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
@JsonDeserialize(builder = ModelsResponse.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModelsResponse {

	@JsonProperty("object")
	private String object;

	@JsonProperty("data")
	private List<Model> data;

	public ModelsResponse(Builder builder) {
		this.object = builder.object;
		this.data = builder.data;
	}

	public static ModelsResponse.Builder builder() {
		return new ModelsResponse.Builder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static final class Builder {

		private String object; // 这里可能会与 Java 的 Object 冲突，可能需要重命名

		private List<Model> data;

		private Builder() {
		}

		public ModelsResponse.Builder object(String object) {
			this.object = object;
			return this;
		}

		public ModelsResponse.Builder data(List<Model> data) {
			this.data = data;
			return this;
		}

		public ModelsResponse build() {
			return new ModelsResponse(this);
		}

	}

}
