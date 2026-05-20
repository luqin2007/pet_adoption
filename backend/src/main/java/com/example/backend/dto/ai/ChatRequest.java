package com.example.backend.dto.ai;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Data
@RequiredArgsConstructor
public class ChatRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatRequest.class);

    private final String prompt;

    private String text;

    private List<String> images;

    public void setText(String format, Object... args) {
        this.text = String.format(format, args);
    }

    public void addImage(Path imagePath) {
        if (images == null)
            images = new ArrayList<>();
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            images.add("data:image/jpeg;base64," + base64);
        } catch (Exception e) {
            LOGGER.warn("Failed to load image {}: {}", imagePath, e.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(String model) {
        List<Map<String, Object>> contents = new ArrayList<>();
        // text
        contents.add(Map.of("type", "text", "text", text));
        // image
        if (images != null && !images.isEmpty()) {
            for (String image : images) {
                contents.add(Map.of(
                        "type", "image_url",
                        "image_url", Map.of("url", image)
                ));
            }
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", prompt),
                Map.of("role", "user", "content", contents)
        ));
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 2000);
        return requestBody;
    }

    public HttpEntity<Map<String, Object>> buildRequest(String apiKey, String model) {
        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        // request
        Map<String, Object> requestBody = buildRequestBody(model);
        return new HttpEntity<>(requestBody, headers);
    }
}
