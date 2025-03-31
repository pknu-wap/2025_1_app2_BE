package com.wap.app2.gachitayo.repository.location;

import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface StopoverRepository extends JpaRepository<Stopover, Long> {
    Optional<Stopover> findByLocationAndStopoverType(Location location, LocationType stopoverType);
}
