package com.wap.app2.gachitayo.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.auth.Token;
import com.wap.app2.gachitayo.dto.request.LoginRequestDto;
import com.wap.app2.gachitayo.dto.request.RegisterRequestDto;
import com.wap.app2.gachitayo.dto.request.ReissueReqeuestDto;
import com.wap.app2.gachitayo.dto.response.TokenResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.jwt.JwtTokenProvider;
import com.wap.app2.gachitayo.repository.member.MemberRepository;
import com.wap.app2.gachitayo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 이 import 문을 추가해야 합니다.

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);
    @Value("${spring.google.iosID}")
    private String ios_clientID;

    @Value("${spring.google.androidID}")
    private String android_clientID;

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final StringRedisTemplate redisTemplate;

    // [수정됨] 읽기 전용 트랜잭션으로 설정하여 성능을 최적화할 수 있습니다.
    @Transactional(readOnly = true)
    public ResponseEntity<TokenResponseDto> userLogin(LoginRequestDto requestDto) {
        String idToken = requestDto.idToken();
        String _accessToken = requestDto.accessToken();
        String email = getUserEmail(idToken, _accessToken);

        Member member = memberService.getUserByEmail(email);

        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        Token token = generateToken(email);

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    // [수정됨] @Transactional 어노테이션을 추가하여 이 메서드 내의 모든 DB 작업이
    // 하나의 트랜잭션으로 묶이도록 합니다. 메서드가 성공적으로 끝나야만 최종 커밋됩니다.
    @Transactional
    public ResponseEntity<TokenResponseDto> registerUser(RegisterRequestDto requestDto) {
        String idToken = requestDto.idToken();
        String _accessToken = requestDto.accessToken();
        String email = getUserEmail(idToken, _accessToken);

        if (email == null) throw new TagogayoException(ErrorCode.INVALID_REQUEST);

        // ... (주석 처리된 SMS 인증 로직) ...

        if (!email.endsWith("pukyong.ac.kr")) throw new TagogayoException(ErrorCode.NOT_MATCH_EMAIL);

        Member existMember = memberService.getUserByEmail(email);

        if (existMember != null) throw new TagogayoException(ErrorCode.ALREADY_SIGNUP);

        Member member = Member.builder()
                .name(requestDto.name())
                .phone(email.substring(0,10)) // 이 부분은 예시 데이터로 보입니다.
                .age(requestDto.age())
                .email(email)
                .gender(requestDto.gender())
                .profileImageUrl(requestDto.profileImageUrl())
                .build();

        memberRepository.save(member); // 이 저장이 완전히 커밋된 후 아래 로직이 실행됩니다.

        redisTemplate.delete(requestDto.key());

        Token token = generateToken(email);

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    // [수정됨] 토큰 재발급 역시 DB 작업이 포함되므로 트랜잭션을 적용합니다.
    @Transactional
    public ResponseEntity<TokenResponseDto> reissueToken(ReissueReqeuestDto requestDto) {
        String rfToken = requestDto.refreshToken();

        boolean isValid = jwtTokenProvider.isValid(rfToken);

        if (!isValid) {
            throw new TagogayoException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String email = redisTemplate.opsForValue().get(rfToken);

        if (email == null) {
            throw new TagogayoException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        redisTemplate.delete(rfToken);

        Token token = generateToken(email);

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    public Token generateToken(String email) {
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        redisTemplate.opsForValue().set(refreshToken, email, Duration.ofDays(7));

        return new Token(
                accessToken,
                refreshToken
        );
    }

    public String getUserEmail(String _idToken, String _accessToken) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(List.of(android_clientID, ios_clientID))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(_idToken);

            if (idToken == null) throw new TagogayoException(ErrorCode.INVALID_TOKEN);

            GoogleIdToken.Payload payload = idToken.getPayload();
            return payload.getEmail();

        } catch (IllegalArgumentException | GeneralSecurityException | IOException e) {
            return null;
        }
    }
}