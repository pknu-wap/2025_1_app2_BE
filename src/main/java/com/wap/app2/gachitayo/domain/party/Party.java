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
    @Column(nullable = false, name="max_person")
    @Builder.Default
    private int maxPeople = 3;

    @NotNull
    @Column(nullable = false, name="radius")
    @Builder.Default
    private double allowRadius = 0.0; // m 단위, 0이면 같은 목적지

    @Column(name="create_time")
    private LocalDateTime createdAt;

    @Column(name="expire_time")
    private LocalDateTime expiredAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GenderOption genderOption = GenderOption.MIXED;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Stopover> stopovers = new ArrayList<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PartyMember> partyMemberList = new ArrayList<>();

    @PrePersist
    public void setTime() {
        if(expiredAt == null && createdAt == null) {
            this.createdAt = LocalDateTime.now();
            this.expiredAt = this.createdAt.plusMinutes(30);
        }
    }
}
