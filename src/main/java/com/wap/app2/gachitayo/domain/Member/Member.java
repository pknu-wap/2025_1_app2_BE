package com.wap.app2.gachitayo.domain.Member;

import com.wap.app2.gachitayo.Enum.Gender;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(length = 10)
    @Builder.Default
    private String name = "익명";

    @NotNull
    @Column(length = 20, unique = true)
    @Builder.Default
    private String phone = "010-0000-0000";

    @NotNull
    @Builder.Default
    private int age = 20;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Gender gender = Gender.MALE;

    @Lob
    @Column(name = "profile_image")
    private String profileImageUrl;

    @NotNull
    @Column(length = 320, unique = true)
    @Builder.Default
    private String email = "anonymous@pukyong.ac.kr";

    @NotNull
    @Builder.Default
    private LocalDateTime created_at = LocalDateTime.now();

    @OneToMany(mappedBy = "member")
    private List<PartyMember> partyMemberList = new ArrayList<>();
}
