package com.wap.app2.gachitayo.domain.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.fare.Fare;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.party.Party;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.ArrayList;

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
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Fare fare;

    @OneToMany(mappedBy = "stopover")
    @Builder.Default
    private List<PaymentStatus> paymentStatusList = new ArrayList<>();

    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LocationType stopoverType = LocationType.STOPOVER;

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

