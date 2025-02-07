package io.github.pigmesh.ai.deepseek.core;

import java.util.Collections;

class AuthorizationHeaderInjector extends GenericHeaderInjector {

    AuthorizationHeaderInjector(String apiKey) {
        super(Collections.singletonMap("Authorization", "Bearer " + apiKey));
    }
}
