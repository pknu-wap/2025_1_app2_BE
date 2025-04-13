package com.wap.app2.gachitayo.repository.party;

import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
    boolean existsByPartyAndMember(Party party, Member member);

    List<PartyMember> findAllByParty(Party party);

    PartyMember findByPartyAndMember(Party party, Member member);
}
