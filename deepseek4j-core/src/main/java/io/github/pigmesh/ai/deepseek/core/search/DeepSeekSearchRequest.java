package io.github.pigmesh.ai.deepseek.core.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

/**
 * DeepSeek 官方 Search API 请求。可选字段为 null 时不发送，由服务端应用默认值并校验参数。
 */
@JsonDeserialize(builder = DeepSeekSearchRequest.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class DeepSeekSearchRequest {

	/**
	 * 搜索查询，必填，1～128 个 Unicode 字符，不能全为空白。
	 */
	@JsonProperty
	private final String query;

	/**
	 * 结果数量，范围 1～50；未提供时服务端默认返回 10 条。
	 */
	@JsonProperty
	private final Integer count;

	/**
	 * 原始问题，可选，1～128 个字符；null 等同未提供。
	 */
	@JsonProperty
	private final String oriQuestion;

	/**
	 * 用于优先返回本地结果的 IP 地址；未提供时服务端默认使用空字符串。
	 */
	@JsonProperty
	private final String localization;

	/**
	 * 是否返回网页正文；未提供时服务端默认为 false。
	 */
	@JsonProperty
	private final Boolean needContent;

	private DeepSeekSearchRequest(Builder builder) {
		this.query = builder.query;
		this.count = builder.count;
		this.oriQuestion = builder.oriQuestion;
		this.localization = builder.localization;
		this.needContent = builder.needContent;
	}

	public String query() {
		return query;
	}

	public Integer count() {
		return count;
	}

	public String oriQuestion() {
		return oriQuestion;
	}

	public String localization() {
		return localization;
	}

	public Boolean needContent() {
		return needContent;
	}

	@Override
	public boolean equals(Object another) {
		if (this == another)
			return true;
		if (!(another instanceof DeepSeekSearchRequest))
			return false;
		DeepSeekSearchRequest that = (DeepSeekSearchRequest) another;
		return Objects.equals(query, that.query) && Objects.equals(count, that.count)
				&& Objects.equals(oriQuestion, that.oriQuestion) && Objects.equals(localization, that.localization)
				&& Objects.equals(needContent, that.needContent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(query, count, oriQuestion, localization, needContent);
	}

	@Override
	public String toString() {
		return "DeepSeekSearchRequest{" + "query=" + query + ", count=" + count + ", oriQuestion=" + oriQuestion
				+ ", localization=" + localization + ", needContent=" + needContent + "}";
	}

	public static Builder builder() {
		return new Builder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static final class Builder {

		private String query;

		private Integer count;

		private String oriQuestion;

		private String localization;

		private Boolean needContent;

		private Builder() {
		}

		public Builder query(String query) {
			this.query = query;
			return this;
		}

		public Builder count(Integer count) {
			this.count = count;
			return this;
		}

		public Builder oriQuestion(String oriQuestion) {
			this.oriQuestion = oriQuestion;
			return this;
		}

		public Builder localization(String localization) {
			this.localization = localization;
			return this;
		}

		public Builder needContent(Boolean needContent) {
			this.needContent = needContent;
			return this;
		}

		public DeepSeekSearchRequest build() {
			return new DeepSeekSearchRequest(this);
		}

	}

}
