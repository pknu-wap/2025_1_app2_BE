package com.wap.app2.gachitayo.error.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    //MEMBER
    ALREADY_SIGNUP(400, "MEMBER-400-1", "이미 회원가입된 회원입니다."),
    INVALID_REQUEST(400, "MEMBER-400-2", "잘못된 요청입니다."),
    NOT_MATCH_EMAIL(400, "MEMBER-400-3", "이메일 주소가 잘못 되었습니다."),
    MEMBER_NOT_LOGIN(403, "USER-403-1", "로그인하지 않았습니다."),
    MEMBER_NOT_FOUND(404, "MEMBER-404-1", "회원을 조회할 수 없습니다."),

    //JWT
    INVALID_JWT(403, "TOKEN-403-1", "잘못된 토큰으로 접근하였습니다."),
    EXPIRED_JWT(403, "TOKEN-403-2", "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(403, "TOKEN-403-3", "리프레시 토큰이 만료되었습니다."),
    TOKEN_MISSING(403, "TOKEN-403-4", "토큰이 없습니다."),

    INTERNAL_SERVER_ERROR(500, "SERVER-500-1", "Internal Server Error");

    private final int status;
    private final String code;
    private final String message;
}
