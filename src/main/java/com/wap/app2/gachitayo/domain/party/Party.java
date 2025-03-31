package com.wap.app2.gachitayo.domain.party;

import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.domain.location.Stopover;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false, name="max_person")
    @Builder.Default
    private Integer maxPerson = 3;

    @Column(nullable = false, name="radius")
    @Builder.Default
    private double allowRadius = 1.0;

    @Enumerated(EnumType.STRING)
    private GenderOption genderOption;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Stopover> stopovers = new ArrayList<>();
}
