package com.wap.app2.gachitayo.jwt;

import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.Member.MemberDetails;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.repository.auth.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;


@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/oauth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer")) {
            log.info("Token Null");
            throw new TagogayoException(ErrorCode.INVALID_JWT);
        }

        String token = authorization.split(" ")[1];

        if (!jwtTokenProvider.isValid(token)) {
            log.info("Token not valid");
            throw new TagogayoException(ErrorCode.INVALID_JWT);
        }

        String email = jwtTokenProvider.getEmailByToken(token);

        Member member = memberRepository.findByEmail(email).orElse(null);

        if (member == null) {
            filterChain.doFilter(request, response);
            return;
        }

        MemberDetails memberDetails = new MemberDetails(member);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(memberDetails, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
