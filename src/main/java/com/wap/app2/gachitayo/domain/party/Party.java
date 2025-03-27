package com.wap.app2.gachitayo.domain.party;

import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.domain.location.Stopover;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder(access = AccessLevel.PROTECTED)
@Entity
public class Party {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name="max_person")
    @Builder.Default
    private Integer maxPerson = 3;

    @Column(nullable = false, name="radius")
    @Builder.Default
    private double allowRadius = 1.0;

    @Enumerated(EnumType.STRING)
    private GenderOption genderOption;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    private List<Stopover> stopovers = new ArrayList<>();

    public Party() {}

    public Party(Integer maxPerson, Double allowRadius, GenderOption genderOption, Stopover stopover) {
        this.maxPerson = maxPerson;
        this.allowRadius = allowRadius;
        this.genderOption = genderOption;
        this.stopovers.add(stopover);
    }
}
