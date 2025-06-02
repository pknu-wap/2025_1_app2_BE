package com.wap.app2.gachitayo.domain.fare;

import com.wap.app2.gachitayo.domain.party.PartyMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saved_money")
public class SavedMoney {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "party_member_id")
    private PartyMember partyMember;

    @NotNull
    @Column(name = "saved_amount")
    private int savedAmount;

    @NotNull
    @Column(name = "base_fare")
    private int baseFare;

    @NotNull
    @Column(name = "final_fare")
    private int finalFare;

    @Builder.Default
    private LocalDateTime confirmed_at = LocalDateTime.now();
} 