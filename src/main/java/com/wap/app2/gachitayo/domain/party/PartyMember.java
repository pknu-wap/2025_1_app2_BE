package com.wap.app2.gachitayo.domain.party;

import com.wap.app2.gachitayo.Enum.PartyMemberRole;
import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "party_member")
public class PartyMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @OneToMany(mappedBy = "partyMember", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentStatus> paymentStatusList = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PartyMemberRole memberRole = PartyMemberRole.MEMBER;
}
