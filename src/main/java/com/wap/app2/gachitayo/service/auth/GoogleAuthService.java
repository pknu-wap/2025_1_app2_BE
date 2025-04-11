package com.wap.app2.gachitayo.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.wap.app2.gachitayo.domain.User.User;
import com.wap.app2.gachitayo.domain.auth.Token;
import com.wap.app2.gachitayo.dto.request.LoginRequestDto;
import com.wap.app2.gachitayo.dto.request.RegisterRequestDto;
import com.wap.app2.gachitayo.dto.request.ReissueReqeuestDto;
import com.wap.app2.gachitayo.dto.response.TokenResponseDto;
import com.wap.app2.gachitayo.jwt.JwtTokenProvider;
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

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public ResponseEntity<TokenResponseDto> userLogin(LoginRequestDto requestDto) {
        String idToken = requestDto.idToken();
        String _accessToken = requestDto.accessToken();
        String email = getUserEmail(idToken, _accessToken);

        User user = getUserByEmail(email);

        if (user == null) return ResponseEntity.internalServerError().build();

        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        Token token = new Token(
                accessToken,
                refreshToken
        );

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    public ResponseEntity<TokenResponseDto> registerUser(RegisterRequestDto requestDto) {
        String idToken = requestDto.idToken();
        String _accessToken = requestDto.accessToken();
        String email = getUserEmail(idToken, _accessToken);

        if (email == null) return ResponseEntity.internalServerError().build();

        //구글 토큰을 검증해서 뒷부분만 확인하면 됨
        if (!email.endsWith("pukyong.ac.kr")) return ResponseEntity.internalServerError().build();

        User existUser = getUserByEmail(email);

        if (existUser != null) return ResponseEntity.internalServerError().build();

        User user = User.builder()
                .name(requestDto.name())
                .phone(requestDto.phone())
                .age(requestDto.age())
                .email(email)
                .profileImageUrl(requestDto.profileImageUrl())
                .build();

        userRepository.save(user);

        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        Token token = new Token(
                accessToken,
                refreshToken
        );

        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    public ResponseEntity<TokenResponseDto> reissueToken(ReissueReqeuestDto requestDto) {
        //db에서 해당 refreshToken 가진사람 가져오기
        //repository.get~~();
        //레디스 사용시 만료 되었는지 확인할 필요없음

        boolean isValid = jwtTokenProvider.isValid(requestDto.refreshToken());

        //Redis 사용시 불필요함
        if (!isValid) return ResponseEntity.internalServerError().build();

        //유저를 디비에서 유저를 가져오는게 아니라 모름...
        Token token = new Token(
            jwtTokenProvider.createAccessToken("test@pukyong.ac.kr"),
            jwtTokenProvider.createRefreshToken()
        );
        return ResponseEntity.ok(TokenResponseDto.from(token));
    }

    public String getUserEmail(String _idToken, String _accessToken) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(_idToken);

            GoogleIdToken.Payload payload = idToken.getPayload();
            return payload.getEmail();

        } catch (IllegalArgumentException | GeneralSecurityException | IOException e) {
            return null;
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
