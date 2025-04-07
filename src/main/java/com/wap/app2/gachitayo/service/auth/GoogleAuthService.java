package com.wap.app2.gachitayo.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.wap.app2.gachitayo.domain.User.User;
import com.wap.app2.gachitayo.domain.auth.Token;
import com.wap.app2.gachitayo.dto.request.LoginRequestDto;
import com.wap.app2.gachitayo.dto.request.RegisterRequestDto;
import com.wap.app2.gachitayo.dto.response.TokenResponseDto;
import com.wap.app2.gachitayo.jwt.JWTUtil;
import com.wap.app2.gachitayo.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    @Value("${spring.google.clientId}")
    private String clientId;

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final Long expiredMs = 12 * 1000L * 60L * 60L; //12시간

    public ResponseEntity<TokenResponseDto> userLogin(LoginRequestDto requestDto) {
        String idToken = requestDto.idToken();
        String _accessToken = requestDto.accessToken();
        String email = getUserEmail(idToken, _accessToken);

        User user = getUserByEmail(email);

        if (user == null) {
            return ResponseEntity.internalServerError().build();
        }

        String accessToken = jwtUtil.createToken(email, expiredMs);
        Token token = new Token(
                accessToken,
                accessToken
        );

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    public ResponseEntity<TokenResponseDto> registerUser(RegisterRequestDto requestDto) {
        String idToken = requestDto.idToken();
        String _accessToken = requestDto.accessToken();
        String email = getUserEmail(idToken, _accessToken);
        String[] _email = email.split("@");

        if (_email.length != 2) return ResponseEntity.internalServerError().build();

        if (!_email[1].equalsIgnoreCase("pukyong.ac.kr")) return ResponseEntity.internalServerError().build();

        User existUser = getUserByEmail(email);

        if (existUser == null) {
            //에러 처리는 어떻게 할까요??
            return ResponseEntity.internalServerError().build();
        }

        User user = User.builder()
                .name(requestDto.name())
                .phone(requestDto.phone())
                .age(requestDto.age())
                .email(email)
                .profileImageUrl(requestDto.profileImageUrl())
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.createToken(email, expiredMs);
        Token token = new Token(
                accessToken,
                accessToken
        );

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }



    public String getUserEmail(String idTokenString, String _accessToken) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                return payload.getEmail();
            } else {
                //에러 처리 핸들링... how to..
                throw new IllegalArgumentException("토큰 검증 실패");
            }

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
