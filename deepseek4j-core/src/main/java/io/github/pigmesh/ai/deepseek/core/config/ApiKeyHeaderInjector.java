package io.github.pigmesh.ai.deepseek.core.config;

import java.util.Collections;

public class ApiKeyHeaderInjector extends GenericHeaderInjector {

    public ApiKeyHeaderInjector(String apiKey) {
        super(Collections.singletonMap("api-key", apiKey));
    }
}
