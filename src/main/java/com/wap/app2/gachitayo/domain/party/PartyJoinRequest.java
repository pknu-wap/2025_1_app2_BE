package com.wap.app2.gachitayo.domain.party;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import com.wap.app2.gachitayo.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class PartyJoinRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    private Party party;

    @Enumerated(EnumType.STRING)
    @Setter
    @Builder.Default
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Setter
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Setter
    private LocalDateTime respondedAt;
}
