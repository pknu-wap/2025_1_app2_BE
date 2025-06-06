package com.wap.app2.gachitayo.domain.party;

import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.review.Review;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Party {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, name="max_person")
    @Builder.Default
    private int maxPeople = 3;

    @NotNull
    @Column(nullable = false, name="radius")
    @Builder.Default
    private double allowRadius = 0.0; // m 단위, 0이면 같은 목적지

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GenderOption genderOption = GenderOption.MIXED;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 5)
    @Builder.Default
    private List<Stopover> stopovers = new ArrayList<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    @BatchSize(size = 4)
    @Builder.Default
    private List<PartyMember> partyMemberList = new ArrayList<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
