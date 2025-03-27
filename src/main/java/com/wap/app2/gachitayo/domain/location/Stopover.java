package com.wap.app2.gachitayo.domain.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.party.Party;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;

@Builder(access = AccessLevel.PROTECTED)
@Entity
public class Stopover {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Location location;

    @Setter(AccessLevel.PROTECTED)
    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @Setter(AccessLevel.PROTECTED)
    @Enumerated(EnumType.STRING)
    private LocationType stopoverType;

    public Stopover() {}

    public Stopover(Location location, Party party, LocationType stopoverType) {
        this.location = location;
        this.party = party;
        this.stopoverType = stopoverType;
    }

}

