package com.wap.app2.gachitayo.repository.location;

import com.wap.app2.gachitayo.domain.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLatitudeAndLongitude(double latitude, double longitude);
}
