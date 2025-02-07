package io.github.pigmesh.ai.deepseek.core;

import java.util.Collections;

class ApiKeyHeaderInjector extends GenericHeaderInjector {

    ApiKeyHeaderInjector(String apiKey) {
        super(Collections.singletonMap("api-key", apiKey));
    }
}
