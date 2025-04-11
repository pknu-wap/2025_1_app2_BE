package com.wap.app2.gachitayo.jwt;

import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTExceptionFilter extends OncePerRequestFilter {

    private static final String errorLogsFormat = """
		{
			"status": "%s",
			"code": "%s",
			"message": "%s"
		}
		""";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (TagogayoException e) {
            ErrorCode errorCode = e.getErrorCode();

            response.setContentType("application/json; charset=UTF-8");

            response.setStatus(errorCode.getStatus());
            response.getWriter().write(errorLogsFormat.formatted(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage()
            ));
        }
    }
}
