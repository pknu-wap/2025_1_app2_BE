package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.fare.Fare;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import com.wap.app2.gachitayo.repository.location.StopoverRepository;
import com.wap.app2.gachitayo.service.fare.FareService;
import lombok.RequiredArgsConstructor;
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
    public void setPaymentStatus(Stopover stopover, PaymentStatus paymentStatus) {
        stopover.getPaymentStatusList().add(paymentStatus);
        stopoverRepository.save(stopover);
    }

    public boolean existWithLocation(Stopover stopover, Location location) {
        return stopover.getLocation().getAddress().equalsIgnoreCase(location.getAddress())
                && stopover.getLocation().getLatitude() == location.getLatitude()
                && stopover.getLocation().getLongitude() == location.getLongitude();
    }

    @Transactional
    public boolean updateStopover(Stopover stopover, LocationDto locationDto, LocationType stopoverType) {
        Location locationEntity = (locationDto != null) ? locationService.createOrGetLocation(locationDto) : null;
        if(!stopover.update(locationEntity, stopoverType)) {
            return false;
        }

        stopoverRepository.save(stopover);
        return true;
    }
}
