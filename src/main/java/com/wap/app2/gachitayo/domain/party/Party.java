package com.wap.app2.gachitayo.domain.party;

import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.domain.location.Stopover;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    @Column(name="max_person")
    @Builder.Default
    private Integer maxPeople = 3;

    @NotNull
    @Column(name="radius")
    @Builder.Default
    private double allowRadius = 0.0; // m 단위, 0 이면 같은 목적지만

    @NotNull
    @Column(name="departure_time")
    @Builder.Default
    private LocalDateTime departureTime = LocalDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GenderOption genderOption = GenderOption.MIXED;

    @OneToMany(mappedBy = "party", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Stopover> stopovers = new ArrayList<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<PartyMember> partyMembers = new ArrayList<>();
}
