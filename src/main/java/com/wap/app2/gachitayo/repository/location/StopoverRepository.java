package com.wap.app2.gachitayo.repository.location;

import com.wap.app2.gachitayo.domain.location.Stopover;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface StopoverRepository extends JpaRepository<Stopover, Long> {
    @Query("""
        SELECT s FROM Stopover s JOIN FETCH s.fare WHERE s.id = :partyId
    """)
    Optional<Stopover> findStopoverWithFare(@Param("partyId") Long partyId);
}
