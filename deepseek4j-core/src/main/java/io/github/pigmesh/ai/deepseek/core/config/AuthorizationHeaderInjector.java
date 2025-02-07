package io.github.pigmesh.ai.deepseek.core.config;

import java.util.Collections;

public class AuthorizationHeaderInjector extends GenericHeaderInjector {

    public AuthorizationHeaderInjector(String apiKey) {
        super(Collections.singletonMap("Authorization", "Bearer " + apiKey));
    }
}
