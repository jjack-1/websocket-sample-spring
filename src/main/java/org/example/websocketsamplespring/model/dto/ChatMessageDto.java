package org.example.websocketsamplespring.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private String nickname;
    private String message;
    private LocalDateTime timestamp;
}
