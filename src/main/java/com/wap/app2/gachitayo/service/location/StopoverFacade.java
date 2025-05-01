package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.fare.Fare;
import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import com.wap.app2.gachitayo.mapper.LocationMapper;
import com.wap.app2.gachitayo.service.fare.FareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StopoverFacade {
    private final LocationService locationService;
    private final StopoverService stopoverService;
    private final FareService fareService;

    private final LocationMapper locationMapper;

    public Stopover createStopover(LocationDto locationDto, LocationType stopoverType) {
        Location location = locationService.createOrGetLocation(locationDto);

        Stopover stopover = Stopover.builder()
                .location(location)
                .stopoverType(stopoverType)
                .build();

        location.addStopover(stopover);
        
        // Hibernate 는 PK 없는 객체는 비영속 상태로 간주하기에 타 엔티티와 연결 전 반드시 save
        Stopover savedStopover = stopoverService.saveStopover(stopover);

        Fare fare = fareService.createDefaultFare(savedStopover);

        savedStopover.setFare(fare);

        return stopoverService.saveStopover(savedStopover); // Fare 연결한 뒤 다시 save
    }

    public Stopover findOrCreateStopover(Party party, LocationDto locationDto) {
        Location location = locationMapper.toEntity(locationDto);

        return party.getStopovers().stream()
                .filter(stopover -> stopover.getLocation().isSameLocation(location))
                .findFirst()
                .orElseGet(() -> {
                    Stopover newStopover = createStopover(locationDto, LocationType.STOPOVER);
                    newStopover.setParty(party);
                    party.getStopovers().add(newStopover);
                    return newStopover;
                });
    }

    public boolean updateStopover(Stopover stopover, LocationDto locationDto, LocationType stopoverType) {
        Location locationEntity = (locationDto != null) ? locationService.createOrGetLocation(locationDto) : null;
        if(!stopover.update(locationEntity, stopoverType)) {
            return false;
        }

        stopoverService.saveStopover(stopover);
        return true;
    }

}
