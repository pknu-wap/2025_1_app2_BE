package com.wap.app2.gachitayo.domain.location;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;

@Builder(access = AccessLevel.PROTECTED)
@Entity
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String locationName;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Setter(AccessLevel.PROTECTED)
    @OneToOne(fetch = FetchType.LAZY)
    private Stopover stopover;

    public Location() {}

    public Location(String locationName, double latitude, double longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
