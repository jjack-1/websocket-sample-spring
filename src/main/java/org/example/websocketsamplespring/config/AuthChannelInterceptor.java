package org.example.websocketsamplespring.config;

import org.example.websocketsamplespring.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private StreamService streamService; // DB대신 스트림 소유권을 확인할 서비스

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 1. 연결 시(CONNECT): JWT 인증 및 세션에 사용자 정보 등록
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 실제로는 헤더의 JWT 토큰을 검증하고 사용자 정보를 가져와야 함
            // 여기서는 시뮬레이션을 위해 임의의 사용자 정보를 생성
            String token = accessor.getFirstNativeHeader("Authorization");

            // 토큰 검증 로직 (예시)
            if (isValidToken(token)) {
                String userId = getUserIdFromToken(token); // 토큰에서 userId 추출
                // 인증된 사용자 정보를 웹소켓 세션의 'user'로 등록
                accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, null));
            }
        }

        // 2. 구독 시(SUBSCRIBE): 채널별 구독 권한 검사
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // 세션에 등록된 사용자 정보가 없으면 구독 거부 (인증되지 않은 사용자)
            if (accessor.getUser() == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }

            String userId = accessor.getUser().getName();
            String destination = accessor.getDestination();
            String streamKey = streamService.extractStreamKeyFromDestination(destination);

            // 2-A. 스트리머 전용 채널('/participants') 구독 시 -> 소유권 검사
            if (destination != null && destination.contains("/participants")) {
                if (!streamService.isStreamOwner(streamKey, userId)) {
                    throw new AccessDeniedException("스트리머만 구독할 수 있는 채널입니다.");
                }
            }
            // 2-B. 채팅 채널은 인증된 사용자라면 누구나 구독 가능 (추가 검사 없음)
        }

        return message;
    }

    // --- JWT 토큰 검증 로직 (예시) ---
    private boolean isValidToken(String token) {
        // 실제 JWT 검증 라이브러리(jjwt 등)를 사용해야 함
        return token != null && token.startsWith("Bearer ");
    }

    private String getUserIdFromToken(String token) {
        // 토큰에서 사용자 ID를 파싱. 여기서는 간단하게 "user-" + 토큰 일부로 시뮬레이션
        return "user-" + token.substring(7, 12);
    }
}
