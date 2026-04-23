package com.example.backend.module;

import com.example.backend.controller.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiControllerContractTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            AdoptBreadingController.class,
            AuthController.class,
            ItemDonationController.class,
            LostPetController.class,
            MedicalCareController.class,
            NoticeController.class,
            PetController.class,
            PublicityController.class,
            RescueTaskController.class,
            UserController.class,
            VolunteerController.class
    );

    @Test
    void openApiDocumentsEveryControllerEndpoint() throws IOException {
        Set<Endpoint> controllerEndpoints = CONTROLLERS.stream()
                .flatMap(controller -> scanController(controller).stream())
                .collect(Collectors.toCollection(TreeSet::new));
        Set<Endpoint> openApiEndpoints = readOpenApiEndpoints(Path.of("openapi.yaml"));

        Set<Endpoint> missingInOpenApi = difference(controllerEndpoints, openApiEndpoints);
        Set<Endpoint> extraInOpenApi = difference(openApiEndpoints, controllerEndpoints);

        assertTrue(missingInOpenApi.isEmpty() && extraInOpenApi.isEmpty(),
                () -> "OpenAPI 与 Controller 路由不一致\n缺少接口:\n"
                        + formatEndpoints(missingInOpenApi)
                        + "\n多余接口:\n"
                        + formatEndpoints(extraInOpenApi));
    }

    private static List<Endpoint> scanController(Class<?> controller) {
        String classPath = firstValue(controller.getAnnotation(RequestMapping.class));
        List<Endpoint> endpoints = new ArrayList<>();
        for (Method method : controller.getDeclaredMethods()) {
            addEndpoint(endpoints, "GET", classPath, method.getAnnotation(GetMapping.class));
            addEndpoint(endpoints, "POST", classPath, method.getAnnotation(PostMapping.class));
            addEndpoint(endpoints, "PUT", classPath, method.getAnnotation(PutMapping.class));
            addEndpoint(endpoints, "PATCH", classPath, method.getAnnotation(PatchMapping.class));
            addEndpoint(endpoints, "DELETE", classPath, method.getAnnotation(DeleteMapping.class));
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (requestMapping != null) {
                for (RequestMethod requestMethod : requestMapping.method()) {
                    endpoints.add(new Endpoint(requestMethod.name(), normalizePath(classPath, firstValue(requestMapping))));
                }
            }
        }
        return endpoints;
    }

    private static void addEndpoint(List<Endpoint> endpoints, String httpMethod, String classPath, Object annotation) {
        if (annotation instanceof GetMapping mapping) {
            endpoints.add(new Endpoint(httpMethod, normalizePath(classPath, firstValue(mapping))));
        } else if (annotation instanceof PostMapping mapping) {
            endpoints.add(new Endpoint(httpMethod, normalizePath(classPath, firstValue(mapping))));
        } else if (annotation instanceof PutMapping mapping) {
            endpoints.add(new Endpoint(httpMethod, normalizePath(classPath, firstValue(mapping))));
        } else if (annotation instanceof PatchMapping mapping) {
            endpoints.add(new Endpoint(httpMethod, normalizePath(classPath, firstValue(mapping))));
        } else if (annotation instanceof DeleteMapping mapping) {
            endpoints.add(new Endpoint(httpMethod, normalizePath(classPath, firstValue(mapping))));
        }
    }

    private static String firstValue(RequestMapping mapping) {
        if (mapping == null) return "";
        return firstNonEmpty(mapping.path(), mapping.value());
    }

    private static String firstValue(GetMapping mapping) {
        return firstNonEmpty(mapping.path(), mapping.value());
    }

    private static String firstValue(PostMapping mapping) {
        return firstNonEmpty(mapping.path(), mapping.value());
    }

    private static String firstValue(PutMapping mapping) {
        return firstNonEmpty(mapping.path(), mapping.value());
    }

    private static String firstValue(PatchMapping mapping) {
        return firstNonEmpty(mapping.path(), mapping.value());
    }

    private static String firstValue(DeleteMapping mapping) {
        return firstNonEmpty(mapping.path(), mapping.value());
    }

    private static String firstNonEmpty(String[] first, String[] second) {
        if (first.length > 0) return first[0];
        if (second.length > 0) return second[0];
        return "";
    }

    private static Set<Endpoint> readOpenApiEndpoints(Path path) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> document = yaml.load(input);
            Object pathsNode = document.get("paths");
            if (!(pathsNode instanceof Map<?, ?> paths)) {
                return Set.of();
            }

            Set<Endpoint> endpoints = new TreeSet<>();
            for (Map.Entry<?, ?> pathEntry : paths.entrySet()) {
                if (!(pathEntry.getValue() instanceof Map<?, ?> methods)) continue;
                for (Object method : methods.keySet()) {
                    String httpMethod = method.toString().toUpperCase(Locale.ROOT);
                    if (Set.of("GET", "POST", "PUT", "PATCH", "DELETE").contains(httpMethod)) {
                        endpoints.add(new Endpoint(httpMethod, normalizeOpenApiPath(pathEntry.getKey().toString())));
                    }
                }
            }
            return endpoints;
        }
    }

    private static Set<Endpoint> difference(Set<Endpoint> left, Set<Endpoint> right) {
        Set<Endpoint> result = new TreeSet<>(left);
        result.removeAll(right);
        return result;
    }

    private static String normalizePath(String classPath, String methodPath) {
        String joined = ("/" + trimSlashes(classPath) + "/" + trimSlashes(methodPath)).replaceAll("/+", "/");
        if (joined.length() > 1 && joined.endsWith("/")) {
            joined = joined.substring(0, joined.length() - 1);
        }
        return joined;
    }

    private static String normalizeOpenApiPath(String path) {
        String normalized = path.replaceAll("/+", "/");
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String trimSlashes(String value) {
        if (value == null || value.isBlank()) return "";
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private static String formatEndpoints(Set<Endpoint> endpoints) {
        if (endpoints.isEmpty()) return "(无)";
        return endpoints.stream()
                .map(endpoint -> endpoint.method() + " " + endpoint.path())
                .collect(Collectors.joining("\n"));
    }

    private record Endpoint(String method, String path) implements Comparable<Endpoint> {
        @Override
        public int compareTo(Endpoint other) {
            int pathCompare = path.compareTo(other.path);
            if (pathCompare != 0) return pathCompare;
            return method.compareTo(other.method);
        }
    }
}
