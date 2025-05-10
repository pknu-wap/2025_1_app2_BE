package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.repository.party.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class PartyExpirationScheduler {
    private final PartyRepository partyRepository;

    @Scheduled(fixedRate = 60 * 10 * 1000) // 5분마다 실행
    public void deleteExpiredParties() {
        LocalDateTime now = LocalDateTime.now();
        int deleteCount = partyRepository.deleteExpiredParties(now);

        if(deleteCount > 0) {
            log.info("만료 파티 삭제: {} 개 삭제", deleteCount);
        }
    }
}
