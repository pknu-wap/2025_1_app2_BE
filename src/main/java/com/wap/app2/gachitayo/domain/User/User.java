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
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false, length = 11, unique = true)
    private String phone;

    @Column(nullable = false)
    private int age;

    @Lob
    @Column(name = "profile_image")
    private String profileImageUrl;

    @Column(nullable = false, length = 320, unique = true)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime created_at = LocalDateTime.now();

}
