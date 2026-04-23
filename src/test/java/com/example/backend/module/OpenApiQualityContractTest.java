package com.example.backend.module;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiQualityContractTest {

    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");

    @Test
    void everyOpenApiOperationHasTagSummaryAndSuccessResponse() throws IOException {
        Map<String, Object> document = readOpenApi();
        Map<?, ?> paths = requireMap(document.get("paths"), "paths");
        List<String> problems = new ArrayList<>();

        for (Map.Entry<?, ?> pathEntry : paths.entrySet()) {
            Map<?, ?> operations = requireMap(pathEntry.getValue(), pathEntry.getKey() + " operations");
            for (Map.Entry<?, ?> operationEntry : operations.entrySet()) {
                String method = operationEntry.getKey().toString();
                if (!HTTP_METHODS.contains(method)) {
                    continue;
                }
                Map<?, ?> operation = requireMap(operationEntry.getValue(), method + " " + pathEntry.getKey());
                String label = method.toUpperCase(Locale.ROOT) + " " + pathEntry.getKey();
                if (!hasText(operation.get("summary"))) {
                    problems.add(label + " 缺少 summary");
                }
                Object tags = operation.get("tags");
                if (!(tags instanceof List<?> list) || list.isEmpty()) {
                    problems.add(label + " 缺少 tags");
                }
                Map<?, ?> responses = requireMap(operation.get("responses"), label + " responses");
                boolean hasSuccess = responses.keySet().stream()
                        .map(Object::toString)
                        .anyMatch(code -> code.startsWith("2"));
                if (!hasSuccess) {
                    problems.add(label + " 缺少 2xx 响应");
                }
            }
        }

        assertTrue(problems.isEmpty(), () -> "OpenAPI 质量检查失败:\n" + String.join("\n", problems));
    }

    @Test
    void openApiDeclaresReusableSchemasAndBearerAuth() throws IOException {
        Map<String, Object> document = readOpenApi();
        Map<?, ?> components = requireMap(document.get("components"), "components");
        Map<?, ?> schemas = requireMap(components.get("schemas"), "components.schemas");
        Map<?, ?> securitySchemes = requireMap(components.get("securitySchemes"), "components.securitySchemes");

        assertTrue(schemas.containsKey("Result"), "OpenAPI 应声明统一响应 Result schema");
        assertTrue(securitySchemes.containsKey("bearerAuth"), "OpenAPI 应声明 bearerAuth 安全方案");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readOpenApi() throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream input = Files.newInputStream(Path.of("openapi.yaml"))) {
            Object loaded = yaml.load(input);
            assertInstanceOf(Map.class, loaded);
            return (Map<String, Object>) loaded;
        }
    }

    private static Map<?, ?> requireMap(Object value, String name) {
        assertInstanceOf(Map.class, value, () -> name + " 应为对象");
        return (Map<?, ?>) value;
    }

    private static boolean hasText(Object value) {
        return value instanceof String text && !text.isBlank();
    }
}
