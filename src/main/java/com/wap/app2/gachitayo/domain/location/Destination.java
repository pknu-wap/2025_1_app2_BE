package com.wap.app2.gachitayo.domain.location;

import com.wap.app2.gachitayo.domain.party.Party;
import jakarta.persistence.*;

@Entity
public class Destination {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;
    
    // 최종 목적지인지 검증을 위한 속성
    private Boolean isFinal = false;
}
