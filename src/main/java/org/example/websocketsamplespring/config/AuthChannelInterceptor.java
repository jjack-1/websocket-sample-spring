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

        // [디버깅 추가] 어떤 명령이 들어오는지 로그 출력
        System.out.println("=== 인터셉터 진입 ===");
        System.out.println("Command: " + accessor.getCommand());
        System.out.println("Destination: " + accessor.getDestination());

        // 1. 연결 시(CONNECT): JWT 인증 및 세션에 사용자 정보 등록
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            System.out.println("받은 토큰: " + token); // [디버깅 추가]

            if (isValidToken(token)) {
                String userId = getUserIdFromToken(token);
                System.out.println("토큰 검증 성공, userId: " + userId); // [디버깅 추가]
                accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, null));
            } else {
                System.out.println("토큰 검증 실패!"); // [디버깅 추가]
                throw new AccessDeniedException("유효하지 않은 토큰입니다. 연결을 거부합니다.");
            }
        }

        // 2. 구독 시(SUBSCRIBE): 채널별 구독 권한 검사
        else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            System.out.println("구독 요청 처리 중..."); // [디버깅 추가]
            if (accessor.getUser() == null) {
                System.out.println("인증 정보 없음!"); // [디버깅 추가]
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }

            String userId = accessor.getUser().getName();
            String destination = accessor.getDestination();
            System.out.println("사용자: " + userId + ", 구독 대상: " + destination); // [디버깅 추가]
            String streamKey = streamService.extractStreamKeyFromDestination(destination);

            // 2-A. 스트리머 전용 채널('/participants') 구독 시 -> 소유권 검사
            if (destination != null && destination.contains("/participants")) {
                if (!streamService.isStreamOwner(streamKey, userId)) {
                    throw new AccessDeniedException("스트리머만 구독할 수 있는 채널입니다.");
                }
            }
            // 2-B. 채팅 채널은 인증된 사용자라면 누구나 구독 가능 (추가 검사 없음)
        }
        System.out.println("=== 인터셉터 통과 ===");
        return message;
    }

    // --- JWT 토큰 검증 로직 (예시) ---
    private boolean isValidToken(String token) {
        // 실제 JWT 검증 라이브러리(jjwt 등)를 사용해야 함
        return token != null && token.startsWith("Bearer ");
    }

    private String getUserIdFromToken(String token) {
        if (token == null || token.length() <= 7) {
            return "unknown-user";
        }

        // [변경] "Bearer streamer-a1b2c3d4-token"에서 전체 추출
        String tokenPart = token.substring(7); // "streamer-a1b2c3d4-token"

        // 스트리머 토큰인 경우 특별 처리
        if (tokenPart.startsWith("streamer-")) {
            return tokenPart.split("-token")[0]; // "streamer-a1b2c3d4"
        }

        // 일반 사용자 토큰인 경우
        return "user-" + tokenPart.substring(0, Math.min(5, tokenPart.length()));
    }
}
