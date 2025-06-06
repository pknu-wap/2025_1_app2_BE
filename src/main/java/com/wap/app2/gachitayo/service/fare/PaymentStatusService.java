package com.wap.app2.gachitayo.service.fare;

import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.fare.SavedMoney;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.repository.fare.PaymentStatusRepository;
import com.wap.app2.gachitayo.repository.fare.SavedMoneyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentStatusService {
    private final PaymentStatusRepository paymentStatusRepository;
    private final SavedMoneyRepository savedMoneyRepository;

    @Transactional
    public PaymentStatus connectPartyMemberWithStopover(PartyMember partyMember, Stopover stopover) {
        PaymentStatus paymentStatus = PaymentStatus.builder()
                .partyMember(partyMember)
                .stopover(stopover)
                .build();
        return paymentStatusRepository.save(paymentStatus);
    }

    @Transactional(readOnly = true)
    public List<PaymentStatus> findPaymentStatusListByStopoverIn(List<Stopover> stopoverList) {
        return paymentStatusRepository.findAllWithPartyMemberAndMemberByStopoverIn(stopoverList);
    }

    @Transactional
    public void updatePaidStatus(PaymentStatus targetPaymentStatus, boolean paid) {
        targetPaymentStatus.setPaid(paid);
        paymentStatusRepository.save(targetPaymentStatus);
    }

    @Transactional
    public void saveSavedMoney(PaymentStatus paymentStatus) {
        Stopover stopover = paymentStatus.getStopover();
        PartyMember partyMember = paymentStatus.getPartyMember();
        
        int baseFare = stopover.getFare().getBaseFigure();
        int finalFare = paymentStatus.getFinalFigure();
        int savedAmount = baseFare - finalFare;

        SavedMoney savedMoney = SavedMoney.builder()
                .partyMember(partyMember)
                .baseFare(baseFare)
                .finalFare(finalFare)
                .savedAmount(savedAmount)
                .build();

        savedMoneyRepository.save(savedMoney);
    }

    @Transactional
    public boolean updateStopover(PartyMember partyMember, Stopover newStopover) {
        PaymentStatus existingPaymentStatus = paymentStatusRepository.findByPartyMember(partyMember);
        if(existingPaymentStatus == null) return false;
        if(existingPaymentStatus.getStopover().equals(newStopover)) return false;

        Stopover oldStopover = existingPaymentStatus.getStopover();
        oldStopover.getPaymentStatusList().remove(existingPaymentStatus);

        newStopover.getPaymentStatusList().add(existingPaymentStatus);
        existingPaymentStatus.setStopover(newStopover);
        paymentStatusRepository.save(existingPaymentStatus);
        return true;
    }
}
