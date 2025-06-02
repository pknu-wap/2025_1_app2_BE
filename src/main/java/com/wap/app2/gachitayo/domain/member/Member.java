package com.wap.app2.gachitayo.domain.member;

import com.wap.app2.gachitayo.Enum.Gender;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.domain.review.Review;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

@Entity
@Getter
@Setter
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
    @Column(length = 11, unique = true)
    @Builder.Default
    private String phone = "01000000000";

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
    @Builder.Default
    private List<PartyMember> partyMemberList = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> writtenReviews = new ArrayList<>(); //작성한 리뷰

    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> receivedReviews = new ArrayList<>(); //받은 리뷰
}