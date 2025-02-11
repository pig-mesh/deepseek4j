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

	@Builder.Default
	private final FreshnessEnums freshness = FreshnessEnums.NO_LIMIT;

	@Builder.Default
	private final boolean summary = true;

	@Builder.Default
	private final int count = 10;

	@Builder.Default
	private final int page = 1;

}
