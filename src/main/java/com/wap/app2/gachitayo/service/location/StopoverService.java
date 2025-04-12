package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.fare.Fare;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import com.wap.app2.gachitayo.repository.location.StopoverRepository;
import com.wap.app2.gachitayo.service.fare.FareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class StopoverService {
    private final StopoverRepository stopoverRepository;
    private final LocationService locationService;
    private final FareService fareService;

    @Transactional
    public Stopover createStopover(LocationDto locationDto, LocationType stopoverType) {
        Location locationEntity = locationService.createOrGetLocation(locationDto);
        Stopover newStopover = Stopover.builder()
                .location(locationEntity)
                .stopoverType(stopoverType)
                .build();

        locationService.updateStopover(newStopover.getLocation(), newStopover);
        Fare fare = fareService.createDefaultFare(newStopover);
        newStopover.setFare(fare);
        return stopoverRepository.save(newStopover);
    }

    @Transactional
    public void addPaymentStatus(Stopover stopover, PaymentStatus paymentStatus) {
        stopover.getPaymentStatusList().add(paymentStatus);
        stopoverRepository.save(stopover);
    }

    @Transactional
    public void setStopoverToParty(Stopover stopover, Party party) {
        stopover.setParty(party);
        stopoverRepository.save(stopover);
    }

    @Transactional
    public ResponseEntity<?> updateStopover(Stopover stopover, LocationDto locationDto, LocationType stopoverType) {
        Location locationEntity = (locationDto != null) ? locationService.createOrGetLocation(locationDto) : null;
        if(!stopover.update(locationEntity, stopoverType)) {
            return ResponseEntity.ok().body("no changed");
        }

        stopoverRepository.save(stopover);
        return ResponseEntity.ok().body("updated stopover");
    }
}
