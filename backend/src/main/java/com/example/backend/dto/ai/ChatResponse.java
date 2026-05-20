package com.example.backend.dto.ai;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Data
public class ChatResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatResponse.class);

    private Usage usage;

    private List<Choice> choices;

    public String getFirstMessage() {
        try {
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("AI API returned no choices");
            }
            Message message = choices.get(0).getMessage();
            return message.getContent();
        } catch (Exception e) {
            LOGGER.error("Failed to extract AI response content: {}", e.getMessage());
            throw new RuntimeException("解析 AI 响应失败: " + e.getMessage());
        }
    }
}
