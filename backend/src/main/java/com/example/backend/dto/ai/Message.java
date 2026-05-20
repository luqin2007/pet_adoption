package com.example.backend.dto.ai;

import lombok.Setter;

@Setter
public class Message {

    private String content;

    public String getContent() {
        return content == null ? "" : content.trim();
    }
}
