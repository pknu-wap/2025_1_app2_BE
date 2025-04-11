package com.wap.app2.gachitayo.repository.party;

import com.wap.app2.gachitayo.domain.party.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
}
