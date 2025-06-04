package com.wap.app2.gachitayo.config.websocket;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.jwt.JwtTokenProvider;
import com.wap.app2.gachitayo.repository.member.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
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
        String errorLogsFormat = """
                {
                    "status": "%s",
                    "code": "%s",
                    "message": "%s"
                }
                """;
        if (request instanceof ServletServerHttpRequest servletRequest && response instanceof ServletServerHttpResponse servletResponse) {
            String token = servletRequest.getServletRequest().getParameter("token");
            log.info("[WebSocket 연결 요청] token: {}", token);
            if (token != null && jwtTokenProvider.isValid(token)) {
                String email = jwtTokenProvider.getEmailByToken(token);
                Member member = memberRepository.findByEmail(email).orElse(null);
                log.info("[WebSocket 인증 성공] email: {}", email);
                if (member != null) {
                    attributes.put("memberEmail", email); // WebSocket 세션에 저장
                    return true;
                } else {
                    HttpServletResponse _response = servletResponse.getServletResponse();
                    ErrorCode errorCode = ErrorCode.EXPIRED_JWT;
                    _response.setContentType("application/json; charset=UTF-8");

                    _response.setStatus(errorCode.getStatus());
                    _response.getWriter().write(errorLogsFormat.formatted(
                            errorCode.getStatus(),
                            errorCode.getCode(),
                            errorCode.getMessage()
                    ));
                    log.warn("[WebSocket 인증 실패] member 없음 target_token: {}", errorLogsFormat);
                    return false;
                }
            } else {
                HttpServletResponse _response = servletResponse.getServletResponse();
                ErrorCode errorCode = ErrorCode.EXPIRED_JWT;
                _response.setContentType("application/json; charset=UTF-8");

                _response.setStatus(errorCode.getStatus());
                _response.getWriter().write(errorLogsFormat.formatted(
                        errorCode.getStatus(),
                        errorCode.getCode(),
                        errorCode.getMessage()
                ));
                log.warn("[WebSocket 인증 실패] 유효하지 않은 토큰 target_token: {}", token);
                return false;
            }
        }
        log.warn("[WebSocket 연결 오류] 요청 형식이 잘못되었습니다.");
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
