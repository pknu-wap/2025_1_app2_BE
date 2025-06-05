package com.wap.app2.gachitayo.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final Long AccessTokenExpiredMs = Duration.ofHours(1).toMillis(); // 1시간
    private final Long RefreshTokenExpiredMs = Duration.ofDays(7).toMillis(); // 1주일

    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public boolean isValid(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            return expiration.after(new Date()); //유효한 토큰
        } catch (SecurityException | MalformedJwtException e) {
            // 서명 오류, 잘못된 토큰 형식
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            // 토큰 만료
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 토큰
            log.info("지원하지 않는 JWT 토큰입니다.");
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return false;
    }

    public String getEmailByToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String createAccessToken(String email) {
        return Jwts.builder()
            .subject(email)
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
