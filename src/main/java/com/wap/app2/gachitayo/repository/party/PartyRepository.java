package com.wap.app2.gachitayo.repository.party;

import com.wap.app2.gachitayo.domain.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartyRepository extends JpaRepository<Party, Long> {
    @Query(value = """
        SELECT DISTINCT p.* From party p
        JOIN stopover s ON p.id = s.party_id
        JOIN location l ON s.location_id = l.id
        WHERE s.stopover_type = 'DESTINATION' AND ST_Distance_Sphere(point(l.longitude, l.latitude), point(:lng, :lat)) <= :radius
        """, nativeQuery = true)
    List<Party> findPartiesWithRadius(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);

    @Query("""
    SELECT DISTINCT p FROM Party p
    JOIN FETCH p.stopovers s
    WHERE p.id = :partyId
    """)
    Optional<Party> findPartyWithStopovers(@Param("partyId") Long partyId);

    @Query("""
    SELECT DISTINCT p FROM PartyMember pm
    JOIN pm.party p
    WHERE pm.member.id = :memberId
    """)
    List<Party> findPartiesByMember(@Param("memberId") Long memberId);

    @Query("""
    SELECT DISTINCT p FROM Party p
    LEFT JOIN FETCH p.stopovers
    WHERE p IN :parties
    """)
    List<Party> fetchStopoversForParties(@Param("parties") List<Party> parties);

    @Query("""
    SELECT DISTINCT p FROM Party p
    LEFT JOIN FETCH p.partyMemberList pm
    LEFT JOIN FETCH pm.member
    WHERE p IN :parties
    """)
    List<Party> fetchPartyMembersForParties(@Param("parties") List<Party> parties);
}
