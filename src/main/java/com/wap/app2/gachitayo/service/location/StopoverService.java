package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import com.wap.app2.gachitayo.repository.location.StopoverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class StopoverService {
    private final StopoverRepository stopoverRepository;
    private final LocationService locationService;

    @Transactional
    public Stopover createStopover(LocationDto locationDto, LocationType stopoverType) {
        Location locationEntity = locationService.createOrGetLocation(locationDto);
        Stopover newStopover = stopoverRepository
                .save(Stopover.builder()
                    .location(locationEntity)
                    .stopoverType(stopoverType)
                    .build());
        locationService.updateStopover(newStopover.getLocation(), newStopover);
        return newStopover;
    }

    @Transactional
    public void setStopoverToParty(Stopover stopover, Party party) {
        stopover.setParty(party);
        stopoverRepository.save(stopover);
    }

    @Transactional
    public ResponseEntity<?> updateStopover(Stopover stopover, Location location, LocationType stopoverType) {
        if(!stopover.update(location, stopoverType)) {
            return ResponseEntity.ok().body("no changed");
        }

        stopoverRepository.save(stopover);
        return ResponseEntity.ok().body("updated stopover");
    }
}
