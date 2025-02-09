package io.github.pigmesh.ai.deepseek.core.search;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

/**
 * 搜索请求
 *
 * @author lengleng
 * @date 2025/2/9
 */
@Builder
@Data
public class SearchRequest {

    private final boolean enable;

    @Setter
    private String query;

    private final FreshnessEnums freshness = FreshnessEnums.NO_LIMIT;

    private final Boolean summary = Boolean.TRUE;

    private final Integer count = 10;

    private final Integer page = 1;

}
