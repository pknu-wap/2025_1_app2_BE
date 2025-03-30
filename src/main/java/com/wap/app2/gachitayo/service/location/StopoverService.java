package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.repository.location.StopoverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StopoverService {
    private final StopoverRepository stopoverRepository;

    @Transactional
    public Stopover findOrCreateStopover(Location targetLocation, LocationType stopoverType) {
        Optional<Stopover> existingStopover = stopoverRepository
                .findByLocationAndStopoverType(targetLocation, stopoverType);
        return existingStopover.orElseGet(() -> stopoverRepository.save(
                Stopover.builder()
                        .location(targetLocation)
                        .stopoverType(stopoverType)
                        .build()
        ));
    }

    @Transactional
    public void setStopoverToParty(Stopover stopover, Party party) {
        stopover.setParty(party);
        stopoverRepository.save(stopover);
    }
}
