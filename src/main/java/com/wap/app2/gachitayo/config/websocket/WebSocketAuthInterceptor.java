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

import java.io.IOException;
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
        if (request instanceof ServletServerHttpRequest servletRequest && response instanceof ServletServerHttpResponse servletResponse) {
            HttpServletResponse httpServletResponse = servletResponse.getServletResponse();
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
                    log.warn("[WebSocket 인증 실패] member 없음 target_token: {}", token);
                    return writeExceptionHttpResponse(httpServletResponse, ErrorCode.INVALID_REQUEST);
                }
            } else {
                log.warn("[WebSocket 인증 실패] 유효하지 않은 토큰 target_token: {}", token);
                return writeExceptionHttpResponse(httpServletResponse, ErrorCode.INVALID_JWT);
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

    private boolean writeExceptionHttpResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        String errorLogsFormat = """
                {
                    "status": "%s",
                    "code": "%s",
                    "message": "%s"
                }
                """;

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(errorCode.getStatus());
        response.getWriter().write(errorLogsFormat.formatted(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
        ));
        return false;
    }
}
