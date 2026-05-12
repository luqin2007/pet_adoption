package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoticeSendRequest implements IRequest {

    @NotNull(message = "request.notice.receiver")
    private Long receiverId;

    @NotBlank(message = "request.notice.title")
    private String title;

    @NotBlank(message = "request.notice.content")
    private String content;
}
