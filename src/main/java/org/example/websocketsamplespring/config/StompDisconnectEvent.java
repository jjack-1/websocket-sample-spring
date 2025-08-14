package org.example.websocketsamplespring.config;

import org.example.websocketsamplespring.model.dto.ParticipantListDto;
import org.example.websocketsamplespring.model.dto.SystemMessageDto;
import org.example.websocketsamplespring.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class StompDisconnectEvent implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private StreamService streamService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());

        // [변경] 세션에서 userId와 streamKey를 가져오는 것은 동일
        String userId = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        String streamKey = (String) headerAccessor.getSessionAttributes().get("streamKey");

        if (userId != null && streamKey != null) {
            // [변경] 퇴장 로직 처리
            streamService.removeParticipant(streamKey, userId);

            // [변경] 퇴장 알림 전송 (새 DTO 사용)
            SystemMessageDto leaveMessage = new SystemMessageDto("leave", userId + "님이 퇴장했습니다.");
            messagingTemplate.convertAndSend("/sub/" + streamKey + "/chat", leaveMessage);

            // [변경] 참가자 목록 업데이트 (새 DTO 사용)
            ParticipantListDto participantList = new ParticipantListDto(streamService.getParticipants(streamKey));
            messagingTemplate.convertAndSend("/sub/" + streamKey + "/participants", participantList);
        }
    }
}
