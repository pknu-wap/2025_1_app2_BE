package com.wap.app2.gachitayo.domain.member;

import com.wap.app2.gachitayo.Enum.Gender;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Builder.Default
    @Column(length = 10)
    private String name = "익명";

    @NotNull
    @Email
    @Builder.Default
    @Column(unique = true)
    private String email = "anonymous@mail.com";

    @NotNull
    @Builder.Default
    @Column(length = 30)
    private String phone = "010-0000-0000";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Gender gender = Gender.MALE;

    @Column(length = 1500)
    private String profileImage;

    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<PartyMember> partyMembers = new ArrayList<>();
}
