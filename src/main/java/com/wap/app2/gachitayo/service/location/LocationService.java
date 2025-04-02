package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import com.wap.app2.gachitayo.mapper.LocationMapper;
import com.wap.app2.gachitayo.repository.location.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public Location createOrGetLocation(LocationDto locationDto) {
        return locationRepository.findByLatitudeAndLongitude(locationDto.getLatitude(), locationDto.getLongitude())
                .map(existingLocation -> locationMapper.toEntityWithExisting(locationDto, existingLocation))
                .orElseGet(() -> locationRepository.save(locationMapper.toEntity(locationDto)));
    }

    @Transactional
    public void updateStopover(Location location, Stopover stopover) {
        location.getStopovers().add(stopover);
        locationRepository.save(location);
    }
}
