package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.repository.location.StopoverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class StopoverService {
    private final StopoverRepository stopoverRepository;

    public Stopover saveStopover(Stopover stopover) {
        return stopoverRepository.save(stopover);
    }
}
