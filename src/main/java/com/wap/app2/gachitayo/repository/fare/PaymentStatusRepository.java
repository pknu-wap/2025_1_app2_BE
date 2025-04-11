package com.wap.app2.gachitayo.repository.fare;

import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Long> {
    PaymentStatus findByPartyMember(PartyMember partyMember);
}
