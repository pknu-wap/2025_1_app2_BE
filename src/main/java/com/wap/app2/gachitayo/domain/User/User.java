package com.wap.app2.gachitayo.domain.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private int age;

    @Column(name = "profile_image")
    private String profileImageUrl;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime created_at = LocalDateTime.now();

}
