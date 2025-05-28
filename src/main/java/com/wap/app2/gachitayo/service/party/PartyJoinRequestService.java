package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.repository.party.PartyJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PartyJoinRequestService {
    private final PartyJoinRequestRepository partyJoinRequestRepository;

    public boolean isAlreadyRequested(Member requester, Party party, JoinRequestStatus status) {
        return partyJoinRequestRepository.existsByPartyAndRequesterAndStatus(party, requester, status);
    }
}
