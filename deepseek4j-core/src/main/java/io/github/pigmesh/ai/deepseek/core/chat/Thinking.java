package io.github.pigmesh.ai.deepseek.core.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

/**
 * DeepSeek 思考模式配置。
 */
@JsonDeserialize(builder = Thinking.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class Thinking {

	@JsonProperty
	private final String type;

	private Thinking(Builder builder) {
		this.type = builder.type;
	}

	public String type() {
		return type;
	}

	public static Thinking enabled() {
		return builder().type(ThinkingType.ENABLED).build();
	}

	public static Thinking disabled() {
		return builder().type(ThinkingType.DISABLED).build();
	}

	public static Thinking of(boolean enabled) {
		return enabled ? enabled() : disabled();
	}

	@Override
	public boolean equals(Object another) {
		if (this == another)
			return true;
		return another instanceof Thinking && equalTo((Thinking) another);
	}

	private boolean equalTo(Thinking another) {
		return Objects.equals(type, another.type);
	}

	@Override
	public int hashCode() {
		int h = 5381;
		h += (h << 5) + Objects.hashCode(type);
		return h;
	}

	@Override
	public String toString() {
		return "Thinking{" + "type=" + type + "}";
	}

	public static Builder builder() {
		return new Builder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static final class Builder {

		private String type;

		private Builder() {
		}

		public Builder type(ThinkingType type) {
			return type(type.toString());
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Thinking build() {
			return new Thinking(this);
		}

	}

}
