package com.wap.app2.gachitayo.domain.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.party.Party;
import jakarta.persistence.*;
import lombok.*;




@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Stopover {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Setter
    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @Setter
    @Enumerated(EnumType.STRING)
    private LocationType stopoverType;

    public boolean update(Location location, LocationType stopoverType) {
        boolean isUpdated = false;
        if(location != null && !this.location.equals(location)) {
            this.location = location;
            this.location.getStopovers().add(this);
            isUpdated = true;
        }
        if(stopoverType != null && !this.stopoverType.equals(stopoverType)) {
            this.stopoverType = stopoverType;
            isUpdated = true;
        }
        return isUpdated;
    }
}

