package io.github.pigmesh.ai.deepseek.core.completion;

public enum CompletionModel {

    GPT_3_5_TURBO_INSTRUCT("gpt-3.5-turbo-instruct");

    private final String value;

    CompletionModel(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
