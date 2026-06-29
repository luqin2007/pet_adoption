package com.example.backend.dto.ai;

import lombok.Setter;

@Setter
public class Usage {

    private Integer promptTokens;

    private Integer completionTokens;

    public Integer getPromptTokens() {
        return promptTokens == null ? 0 : promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens == null ? 0 : completionTokens;
    }
}
