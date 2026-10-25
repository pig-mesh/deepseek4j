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
 * DeepSeek 官方 Search API 返回的一条搜索结果。
 */
@JsonDeserialize(builder = DeepSeekSearchResult.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public final class DeepSeekSearchResult {

	/**
	 * 相关性得分，范围 0～3。
	 */
	@JsonProperty
	private final Double rerankScore;

	@JsonProperty
	private final String title;

	@JsonProperty
	private final String url;

	/**
	 * 网页短摘要。
	 */
	@JsonProperty
	private final String snippet;

	/**
	 * 网页长摘要。
	 */
	@JsonProperty
	private final String summary;

	/**
	 * 网页正文；未请求正文或正文缺失时为空字符串。
	 */
	@JsonProperty
	private final String content;

	/**
	 * 网站名称，可能为 null。
	 */
	@JsonProperty
	private final String siteName;

	/**
	 * 网站图标 URL，可能为 null。
	 */
	@JsonProperty
	private final String siteIcon;

	/**
	 * UTC RFC 3339 格式的日期，可能为 null。
	 */
	@JsonProperty
	private final String date;

	/**
	 * 网页权威性等级，范围 -1～4，-1 表示未知。
	 */
	@JsonProperty
	private final Integer authority;

	private DeepSeekSearchResult(Builder builder) {
		this.rerankScore = builder.rerankScore;
		this.title = builder.title;
		this.url = builder.url;
		this.snippet = builder.snippet;
		this.summary = builder.summary;
		this.content = builder.content;
		this.siteName = builder.siteName;
		this.siteIcon = builder.siteIcon;
		this.date = builder.date;
		this.authority = builder.authority;
	}

	public Double rerankScore() {
		return rerankScore;
	}

	public String title() {
		return title;
	}

	public String url() {
		return url;
	}

	public String snippet() {
		return snippet;
	}

	public String summary() {
		return summary;
	}

	public String content() {
		return content;
	}

	public String siteName() {
		return siteName;
	}

	public String siteIcon() {
		return siteIcon;
	}

	public String date() {
		return date;
	}

	public Integer authority() {
		return authority;
	}

	@Override
	public boolean equals(Object another) {
		if (this == another)
			return true;
		if (!(another instanceof DeepSeekSearchResult))
			return false;
		DeepSeekSearchResult that = (DeepSeekSearchResult) another;
		return Objects.equals(rerankScore, that.rerankScore) && Objects.equals(title, that.title)
				&& Objects.equals(url, that.url) && Objects.equals(snippet, that.snippet)
				&& Objects.equals(summary, that.summary) && Objects.equals(content, that.content)
				&& Objects.equals(siteName, that.siteName) && Objects.equals(siteIcon, that.siteIcon)
				&& Objects.equals(date, that.date) && Objects.equals(authority, that.authority);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rerankScore, title, url, snippet, summary, content, siteName, siteIcon, date, authority);
	}

	@Override
	public String toString() {
		return "DeepSeekSearchResult{" + "rerankScore=" + rerankScore + ", title=" + title + ", url=" + url
				+ ", snippet=" + snippet + ", summary=" + summary + ", content=" + content + ", siteName=" + siteName
				+ ", siteIcon=" + siteIcon + ", date=" + date + ", authority=" + authority + "}";
	}

	public static Builder builder() {
		return new Builder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static final class Builder {

		private Double rerankScore;

		private String title;

		private String url;

		private String snippet;

		private String summary;

		private String content;

		private String siteName;

		private String siteIcon;

		private String date;

		private Integer authority;

		private Builder() {
		}

		public Builder rerankScore(Double rerankScore) {
			this.rerankScore = rerankScore;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder url(String url) {
			this.url = url;
			return this;
		}

		public Builder snippet(String snippet) {
			this.snippet = snippet;
			return this;
		}

		public Builder summary(String summary) {
			this.summary = summary;
			return this;
		}

		public Builder content(String content) {
			this.content = content;
			return this;
		}

		public Builder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public Builder siteIcon(String siteIcon) {
			this.siteIcon = siteIcon;
			return this;
		}

		public Builder date(String date) {
			this.date = date;
			return this;
		}

		public Builder authority(Integer authority) {
			this.authority = authority;
			return this;
		}

		public DeepSeekSearchResult build() {
			return new DeepSeekSearchResult(this);
		}

	}

}
