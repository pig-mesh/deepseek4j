package io.github.pigmesh.ai.deepseek.core.search;

/**
 * @author lengleng
 * @date 2025/2/9
 */
public enum FreshnessEnums {

    ONE_DAY("oneDay"),
    ONE_WEEK("oneWeek"),
    ONE_MONTH("oneMonth"),
    ONE_YEAR("oneYear"),
    NO_LIMIT("noLimit");

    private final String value;

    FreshnessEnums(String value) {
        this.value = value;
    }
}
