package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.domain.auth.Token;

public record TokenResponseDto(
        String accessToken,
        String refreshToken
) {
    public static TokenResponseDto from(Token token) {
        return new TokenResponseDto(token.accessToken(), token.refreshToken());
    }
}
