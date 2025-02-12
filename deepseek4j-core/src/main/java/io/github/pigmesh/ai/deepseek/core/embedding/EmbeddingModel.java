package io.github.pigmesh.ai.deepseek.core.embedding;

public enum EmbeddingModel {

	BGE_M3("bge-m3:latest");

	private final String value;

	EmbeddingModel(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

}
