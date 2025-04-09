package com.wap.app2.gachitayo.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final Long AccessTokenExpiredMs = 7 * 24 * 1000L * 60L * 60L; //1시간 -> 리프레쉬 토큰 구현전 1주일로 변경
    private final Long RefreshTokenExpiredMs = 7 * 24 * 1000L * 60L * 60L; //1주일

    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public Boolean isExpired(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        return expiration.before(new Date()); // 만료일이 지금보다 전이면 만료됨
    }

    public String createAccessToken(String email) {
        return Jwts.builder()
            .claim("email", email)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + AccessTokenExpiredMs))
            .signWith(this.secretKey)
            .compact();
    }

    public String createRefreshToken() {
        return Jwts.builder()
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + RefreshTokenExpiredMs))
            .signWith(this.secretKey)
            .compact();
    }
}
