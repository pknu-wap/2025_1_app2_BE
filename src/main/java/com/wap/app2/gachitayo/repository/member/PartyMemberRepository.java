package com.wap.app2.gachitayo.repository.member;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    List<PartyMember> findAllByParty(Party party);
    PartyMember findByPartyAndMember(Party party, Member member);
    @Query("""
        SELECT pm FROM PartyMember pm
        JOIN FETCH pm.member m
        JOIN FETCH pm.paymentStatusList ps
        JOIN FETCH ps.stopover s
        JOIN FETCH s.location l
        JOIN FETCH s.fare f
        WHERE pm.party.id = :partyId
    """)
    List<PartyMember> findWithAllDetailsByPartyId(@Param("partyId") Long partyId);

    boolean existsByPartyAndMember(Party party, Member member);
}
