package com.wap.app2.gachitayo.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TagogayoException extends RuntimeException {
    private final ErrorCode errorCode;
}