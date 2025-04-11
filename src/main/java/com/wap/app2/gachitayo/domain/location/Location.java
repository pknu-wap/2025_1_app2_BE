package com.wap.app2.gachitayo.domain.location;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    @Builder.Default
    private String address = "";

    @Setter
    @NotNull
    private double latitude;

    @Setter
    @NotNull
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
