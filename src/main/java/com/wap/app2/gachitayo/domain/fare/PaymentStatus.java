package com.wap.app2.gachitayo.domain.fare;

import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_status")
public class PaymentStatus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "party_member_id")
    private PartyMember partyMember;

    @NotNull
    @ManyToOne
    @Setter
    @JoinColumn(name = "stopover_id")
    private Stopover stopover;

    @NotNull
    @Builder.Default
    private boolean isPaid = false;
}
