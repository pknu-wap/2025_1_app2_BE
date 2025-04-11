package com.wap.app2.gachitayo.error;

import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String errorLogsFormat = """
		{
			"status": "%s",
			"code": "%s",
			"message": "%s"
		}
		""";

    @ExceptionHandler(TagogayoException.class)
    public ResponseEntity<ErrorResponse> handleGlobal(TagogayoException error) {
        final ErrorCode errorCode = error.getErrorCode();
        log.error(
                errorLogsFormat.formatted(
                        errorCode.getStatus(),
                        errorCode.getCode(),
                        errorCode.getMessage()
                )
        );
        return new ResponseEntity<>(
                new ErrorResponse(
                        errorCode.getStatus(),
                        errorCode.getCode(),
                        errorCode.getMessage()),
                HttpStatus.valueOf(errorCode.getStatus())
        );
    }
}
