package org.example.websocketsamplespring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private AuthChannelInterceptor authChannelInterceptor; // 새로 만든 인터셉터 주입

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // [변경] 클라이언트 -> 서버로 발행(Publish)할 때의 prefix
        config.setApplicationDestinationPrefixes("/pub");

        // [변경] 서버 -> 클라이언트로 구독(Subscribe) 정보를 보낼 때의 prefix
        config.enableSimpleBroker("/sub");

        // [추가] 1:1 메시징(private message)을 위한 prefix. 기본값은 /user지만 명시적으로 설정.
        config.setUserDestinationPrefix("/user");
    }

    // [추가] 모든 STOMP 메시지 처리 전, 우리가 만든 인터셉터를 거치도록 설정
    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
