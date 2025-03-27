package com.wap.app2.gachitayo.domain.party;

import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.domain.location.Stopover;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Party {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name="max_person")
    private Integer maxParticipants;

    @Column(nullable = false, name="radius")
    private double allowRadius = 1.0;

    @Enumerated(EnumType.STRING)
    private GenderOption genderOption;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    private List<Stopover> stopovers = new ArrayList<>();
}
