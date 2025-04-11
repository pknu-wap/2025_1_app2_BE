package com.wap.app2.gachitayo.config;

import com.wap.app2.gachitayo.jwt.JWTFilter;
import com.wap.app2.gachitayo.jwt.JwtTokenProvider;
import com.wap.app2.gachitayo.repository.auth.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.formLogin(FormLoginConfigurer::disable);

        http.httpBasic(HttpBasicConfigurer::disable);

        http.authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/api/oauth/**").permitAll()
                        .anyRequest().authenticated()
        ).addFilterBefore(new JWTFilter(jwtTokenProvider, memberRepository), UsernamePasswordAuthenticationFilter.class);

        http.sessionManagement((session)
                -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
