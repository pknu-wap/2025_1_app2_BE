package com.wap.app2.gachitayo.repository.party;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyJoinRequestRepository extends JpaRepository<PartyJoinRequest, Long> {
    boolean existsByPartyAndRequesterAndStatus(Party party, Member requester, JoinRequestStatus status);
}
