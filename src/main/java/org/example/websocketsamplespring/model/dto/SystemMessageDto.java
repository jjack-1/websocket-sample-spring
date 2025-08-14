package org.example.websocketsamplespring.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemMessageDto {
    private String type; // "join", "leave"
    private String message;
}
