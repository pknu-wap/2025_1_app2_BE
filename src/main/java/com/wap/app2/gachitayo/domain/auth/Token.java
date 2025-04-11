package com.wap.app2.gachitayo.domain.auth;

public record Token(
        String accessToken,
        String refreshToken
) {
}
