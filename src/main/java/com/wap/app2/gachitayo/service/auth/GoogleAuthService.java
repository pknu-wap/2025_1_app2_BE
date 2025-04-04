package com.wap.app2.gachitayo.service.auth;

import com.wap.app2.gachitayo.domain.User.User;
import com.wap.app2.gachitayo.domain.auth.Token;
import com.wap.app2.gachitayo.dto.request.LoginRequestDto;
import com.wap.app2.gachitayo.dto.request.RegisterRequestDto;
import com.wap.app2.gachitayo.dto.response.TokenResponseDto;
import com.wap.app2.gachitayo.jwt.JWTUtil;
import com.wap.app2.gachitayo.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public ResponseEntity<TokenResponseDto> getUserDetail(LoginRequestDto requestDto) {
        return ResponseEntity.ok(TokenResponseDto.from(new Token(jwtUtil.createToken("email"), jwtUtil.createToken("test"))));
    }

    public ResponseEntity<TokenResponseDto> registerUser(RegisterRequestDto requestDto) {
        User user = User.builder()
                .name(requestDto.name())
                .phone(requestDto.phone())
                .age(requestDto.age())
                .email("test@gmail.com")
                .profileImageUrl(requestDto.profileImageUrl())
                .build();

        ResponseEntity<TokenResponseDto> test = getUserDetail(new LoginRequestDto(requestDto.idToken(), requestDto.accessToken()));

        userRepository.save(user);
        return test;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
