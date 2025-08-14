package org.example.websocketsamplespring.model;

import lombok.AllArgsConstructor;
import lombok.Data;

// 나중에 User DTO 를 이걸로 사용하면 됨
@Data
@AllArgsConstructor
public class Participant {
    private String userId;
    private String nickname;
}
