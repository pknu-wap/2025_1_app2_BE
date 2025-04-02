package com.wap.app2.gachitayo.domain.location;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String address;

    @Setter
    @Column(nullable = false)
    private double latitude;

    @Setter
    @Column(nullable = false)
    private double longitude;

    @OneToMany(mappedBy = "location")
    @Builder.Default
    private List<Stopover> stopovers = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
