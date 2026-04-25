# Repository Guidelines

## Project Structure & Module Organization

This is a multi-module Maven Java project for the DeepSeek Java SDK:

- `deepseek4j-core/`: core client, request/response models, Retrofit/OkHttp integration, streaming handlers, and API abstractions.
- `deepseek-spring-boot-starter/`: Spring Boot auto-configuration, properties, prompt resources, and starter tests.
- `deepseek-solon-plugin/`: Solon plugin configuration, properties, prompt resources, and example test application.
- `deepseek4j-example/`: runnable Spring Boot sample with DeepSeek and Ollama controllers plus profile-specific YAML files.
- `sse.html`: standalone browser page for testing SSE endpoints.

Java sources live under `src/main/java`, tests under `src/test/java`, and runtime resources under `src/main/resources`.

## Build, Test, and Development Commands

- `mvn clean install`: builds all modules and runs tests.
- `mvn test`: runs the full test suite.
- `mvn -pl deepseek4j-core test`: tests one module.
- `mvn -pl deepseek4j-example spring-boot:run`: starts the example app.
- `mvn spring-javaformat:apply`: applies Spring Java Format.
- `mvn -Prelease clean package`: builds release artifacts; requires release credentials and GPG setup.

Use Java 8 compatibility unless the parent POM is changed.

## Coding Style & Naming Conventions

Follow `.editorconfig`: UTF-8, LF endings, tabs size 4 for Java/XML, and 2-space indentation for HTML/JS. Java formatting is managed by `io.spring.javaformat:spring-javaformat-maven-plugin`.

Keep packages under `io.github.pigmesh.ai.deepseek`. Use `UpperCamelCase` for classes and enums, `lowerCamelCase` for fields and methods, and descriptive suffixes such as `Request`, `Response`, `Properties`, `AutoConfiguration`, and `Client`. Preserve `lombok.config`; it is required for constructor injection annotation copying.

## Testing Guidelines

Tests use JUnit Jupiter, Spring Boot Test, AssertJ, MockWebServer, and module-specific framework dependencies. Name tests `*Test.java` and place them in the module they cover.

Prefer focused tests for serialization, configuration binding, client construction, and streaming behavior. Run `mvn test` before opening a pull request; for scoped changes, also run the relevant `mvn -pl <module> test`.

## Commit & Pull Request Guidelines

Recent commits mostly follow Conventional Commits with scopes, for example `feat(core): ...`, `docs(readme): ...`, `chore(config): ...`, and `refactor(core): ...`. Use short imperative subjects and align scopes with modules or areas.

Pull requests should include a concise description, affected modules, test results, and any configuration or API compatibility notes. Link related issues when available. Include screenshots only for changes affecting `sse.html` or example UI behavior.

## Security & Configuration Tips

Do not commit API keys or local credentials. Keep secrets in local YAML overrides, environment variables, or runtime configuration. When editing examples, use placeholder values such as `your-api-key-here`.
