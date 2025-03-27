package com.wap.app2.gachitayo.domain.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.party.Party;
import jakarta.persistence.*;

@Entity
public class Stopover {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @Enumerated(EnumType.STRING)
    private LocationType stopoverType;
}
