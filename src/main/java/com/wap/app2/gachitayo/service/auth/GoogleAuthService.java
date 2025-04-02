package com.wap.app2.gachitayo.service.auth;

import com.wap.app2.gachitayo.domain.auth.Token;
import com.wap.app2.gachitayo.dto.response.TokenResponseDto;
import com.wap.app2.gachitayo.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final JWTUtil jwtUtil;

    public ResponseEntity<TokenResponseDto> getUserDetail() {
        return ResponseEntity.ok(TokenResponseDto.from(new Token(jwtUtil.createToken("email"), jwtUtil.createToken("test"))));
    }
}
