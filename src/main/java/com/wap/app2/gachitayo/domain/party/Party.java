package com.wap.app2.gachitayo.domain.party;

import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.domain.location.Destination;
import com.wap.app2.gachitayo.domain.location.Location;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Party {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    private GenderOption genderOption;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location startLocation;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    private List<Destination> destinations = new ArrayList<>();
}
