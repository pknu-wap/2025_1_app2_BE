package com.wap.app2.gachitayo.config.websocket;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.jwt.JwtTokenProvider;
import com.wap.app2.gachitayo.repository.member.MemberRepository;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public WebSocketAuthInterceptor(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && jwtTokenProvider.isValid(token)) {
                String email = jwtTokenProvider.getEmailByToken(token);
                Member member = memberRepository.findByEmail(email).orElse(null);
                if (member != null) {
                    attributes.put("memberEmail", email); // WebSocket 세션에 저장
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        if (exception != null) {
            System.out.println("Websocket Handshake Error");
            exception.printStackTrace();
        }
    }
}
