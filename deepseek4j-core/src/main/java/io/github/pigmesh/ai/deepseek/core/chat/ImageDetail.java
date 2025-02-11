package io.github.pigmesh.ai.deepseek.core.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ImageDetail {

	@JsonProperty("low")
	LOW, @JsonProperty("high")
	HIGH, @JsonProperty("auto")
	AUTO

}
