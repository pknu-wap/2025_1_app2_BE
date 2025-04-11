package com.wap.app2.gachitayo.service.fare;

import com.wap.app2.gachitayo.domain.fare.Fare;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.repository.fare.FareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FareService {
    private final FareRepository fareRepository;

    @Transactional
    public Fare createDefaultFare(Stopover stopover) {
        Fare defaultFare = Fare.builder()
                .stopover(stopover)
                .baseFigure(0)
                .finalFigure(0)
                .build();
        return fareRepository.save(defaultFare);
    }
}
