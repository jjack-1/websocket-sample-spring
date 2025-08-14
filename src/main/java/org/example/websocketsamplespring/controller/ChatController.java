package org.example.websocketsamplespring.controller;

import org.example.websocketsamplespring.model.dto.ChatMessageDto;
import org.example.websocketsamplespring.model.dto.ParticipantListDto;
import org.example.websocketsamplespring.model.dto.SystemMessageDto;
import org.example.websocketsamplespring.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private StreamService streamService;

    // [변경] 사용자가 채팅방에 참여했을 때 호출
    @MessageMapping("/stream/{streamKey}/join")
    public void handleJoin(@DestinationVariable String streamKey, SimpMessageHeaderAccessor headerAccessor) {
        Authentication user = (Authentication) headerAccessor.getUser();
        String userId = user.getName();
        String nickname = "User-" + userId.substring(5); // 닉네임 생성 로직(예시)

        // 1. 참가자 목록에 추가 (비즈니스 로직)
        streamService.addParticipant(streamKey, userId, nickname);

        // 2. 세션에 "어떤 방에 참여했는지" 정보 저장 (퇴장 시 사용)
        headerAccessor.getSessionAttributes().put("streamKey", streamKey);

        // 3. 채팅방 전체에 입장 알림 전송
        SystemMessageDto joinMessage = new SystemMessageDto("join", nickname + "님이 입장했습니다.");
        messagingTemplate.convertAndSend("/sub/" + streamKey + "/chat", joinMessage);

        // 4. 스트리머에게만 최신 참가자 목록 전송
        updateAndSendParticipantList(streamKey);
    }

    // [변경] 채팅 메시지 처리
    @MessageMapping("/stream/{streamKey}/chat")
    public void handleChatMessage(@DestinationVariable String streamKey, ChatMessageDto chatMessage) {
        messagingTemplate.convertAndSend("/sub/" + streamKey + "/chat", chatMessage);
    }

    // [변경] 참가자 목록을 보내는 헬퍼 메서드
    private void updateAndSendParticipantList(String streamKey) {
        ParticipantListDto participantList = new ParticipantListDto(streamService.getParticipants(streamKey));
        // 스트리머 전용 구독 주소로 메시지 전송
        messagingTemplate.convertAndSend("/sub/" + streamKey + "/participants", participantList);
    }
}
