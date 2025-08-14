package org.example.websocketsamplespring.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// 범용 메시지 포맷
@Data
public class Message {
    private String type;
    private String sender;
    private String userId;
    private String nickname;
    private String message;
    private String streamKey;
    private List<Participant> participants;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
