package com.wap.app2.gachitayo.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    private final SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ((SecretKey) Jwts.SIG.HS256.key().build()).getAlgorithm());
    }

    public Boolean isExpired() {
        return true;
    }

    public String createToken(String email) {
        return Jwts.builder().claim("email", email).issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + 12312313)).signWith(this.secretKey).compact();
    }
}
