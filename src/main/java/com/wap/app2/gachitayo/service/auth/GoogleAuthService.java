package com.wap.app2.gachitayo.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.auth.Token;
import com.wap.app2.gachitayo.dto.request.LoginRequestDto;
import com.wap.app2.gachitayo.dto.request.RegisterRequestDto;
import com.wap.app2.gachitayo.dto.request.ReissueReqeuestDto;
import com.wap.app2.gachitayo.dto.response.TokenResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.jwt.JwtTokenProvider;
import com.wap.app2.gachitayo.repository.auth.MemberRepository;
import com.wap.app2.gachitayo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    @Value("${spring.google.iosID}")
    private String ios_clientID;

    @Value("${spring.google.androidID}")
    private String android_clientID;

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final StringRedisTemplate redisTemplate;

    public ResponseEntity<TokenResponseDto> userLogin(LoginRequestDto requestDto) {
        String idToken = requestDto.idToken();
        String _accessToken = requestDto.accessToken();
        String email = getUserEmail(idToken, _accessToken);

        Member member = memberService.getUserByEmail(email);

        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        Token token = generateToken(email);

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    public ResponseEntity<TokenResponseDto> registerUser(RegisterRequestDto requestDto) {
        String idToken = requestDto.idToken();
        String _accessToken = requestDto.accessToken();
        String email = getUserEmail(idToken, _accessToken);

        if (email == null) throw new TagogayoException(ErrorCode.INVALID_REQUEST);

        //구글 토큰을 검증해서 뒷부분만 확인하면 됨
        if (!email.endsWith("pukyong.ac.kr")) throw new TagogayoException(ErrorCode.NOT_MATCH_EMAIL);

        Member existMember = memberService.getUserByEmail(email);

        if (existMember != null) throw new TagogayoException(ErrorCode.ALREADY_SIGNUP);

        Member member = Member.builder()
                .name(requestDto.name())
                .phone(requestDto.phone())
                .age(requestDto.age())
                .email(email)
                .gender(requestDto.gender())
                .profileImageUrl(requestDto.profileImageUrl())
                .build();

        memberRepository.save(member);

        Token token = generateToken(email);

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    public ResponseEntity<TokenResponseDto> reissueToken(ReissueReqeuestDto requestDto) {
        //RTR 방식 도입으로 토큰 탈취에 대한 보안강화 예정
        String rfToken = requestDto.refreshToken();

        boolean isValid = jwtTokenProvider.isValid(rfToken);

        if (!isValid) {
            //rf expired
            throw new TagogayoException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String email = redisTemplate.opsForValue().get(rfToken);

        if (email == null) {
            //rf expired, 토큰 검증 완료되었는데 db 케이스는 존재할 수 없음.
            throw new TagogayoException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        Token token = generateToken(email);

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    public Token generateToken(String email) {
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        //TTL 일주일로 레디스 저장
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
