package com.wap.app2.gachitayo.service.fare;

import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.repository.fare.PaymentStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentStatusService {
    private final PaymentStatusRepository paymentStatusRepository;

    @Transactional
    public PaymentStatus linkWithPartyMemberAndStopover(PartyMember partyMember, Stopover stopover) {
        PaymentStatus paymentStatus = PaymentStatus.builder()
                .partyMember(partyMember)
                .stopover(stopover)
                .build();
        return paymentStatusRepository.save(paymentStatus);
    }

    public boolean updateStopover(PartyMember partyMember, Stopover stopover) {
        PaymentStatus existingPaymentStatus = paymentStatusRepository.findByPartyMember(partyMember);
        if(existingPaymentStatus == null) {
            return false;
        }
        if(existingPaymentStatus.getStopover().equals(stopover)) {
            return false;
        }
        existingPaymentStatus.setStopover(stopover);
        paymentStatusRepository.save(existingPaymentStatus);
        return true;
    }
}
