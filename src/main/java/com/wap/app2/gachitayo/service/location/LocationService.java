package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import com.wap.app2.gachitayo.mapper.LocationMapper;
import com.wap.app2.gachitayo.repository.location.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public Location createOrGetLocation(LocationDto locationDto) {
        return locationRepository.findByLatitudeAndLongitude(locationDto.getLatitude(), locationDto.getLongitude())
                .orElseGet(() -> locationRepository.save(locationMapper.toEntity(locationDto)));
    }
}
