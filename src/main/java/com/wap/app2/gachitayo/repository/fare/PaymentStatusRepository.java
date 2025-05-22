package com.wap.app2.gachitayo.repository.fare;

import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Long> {
    PaymentStatus findByPartyMember(@NotNull PartyMember partyMember);

    @Query("""
        SELECT ps FROM PaymentStatus ps
        JOIN FETCH ps.partyMember pm
        JOIN FETCH pm.member
        WHERE ps.stopover IN :stopovers
    """)
    List<PaymentStatus> findAllWithPartyMemberAndMemberByStopoverIn(List<Stopover> stopovers);
}
