package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyJoinRequest;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.repository.party.PartyJoinRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class PartyJoinRequestService {
    private final PartyJoinRequestRepository partyJoinRequestRepository;

    @Transactional(readOnly = true)
    public boolean isAlreadyRequested(Member requester, Party party, JoinRequestStatus status) {
        return partyJoinRequestRepository.existsByPartyAndRequesterAndStatus(party, requester, status);
    }

    @Transactional(readOnly = true)
    public PartyJoinRequest findJoinRequestById(Long requestId) {
        return partyJoinRequestRepository.findById(requestId).orElseThrow(() -> new TagogayoException(ErrorCode.JOIN_REQUEST_NOT_FOUND));
    }

    public void requestJoin(Member requester, Party party, JoinRequestStatus status) {
        partyJoinRequestRepository.save(PartyJoinRequest.builder()
                .requester(requester)
                .party(party)
                .status(status)
                .requestedAt(LocalDateTime.now())
                .build());
    }
}
