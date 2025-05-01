package com.wap.app2.gachitayo.repository.party;

import com.wap.app2.gachitayo.domain.party.Party;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {
    @Query(value = """
        SELECT DISTINCT p.* From party p
        JOIN stopover s ON p.id = s.party_id
        JOIN location l ON s.location_id = l.id
        WHERE s.stopover_type = 'DESTINATION' AND ST_Distance_Sphere(point(l.longitude, l.latitude), point(:lng, :lat)) <= :radius
        """, nativeQuery = true)
    List<Party> findPartiesWithRadius(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);

    @Modifying
    @Transactional
    @Query("DELETE FROM Party p WHERE p.createdAt <= :expiredAt")
    int deleteExpiredParties(@Param("expiredAt") LocalDateTime expiredAt);
}
