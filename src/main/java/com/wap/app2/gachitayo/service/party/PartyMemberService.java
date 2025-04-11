package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.PartyMemberRole;
import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.repository.party.PartyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartyMemberService {
    private final PartyMemberRepository partyMemberRepository;

    @Transactional
    public PartyMember connectMemberWithParty(Party party, Member member, PartyMemberRole role) {
        return partyMemberRepository.save(PartyMember.builder()
                .party(party)
                .member(member)
                .memberRole(role)
                .build());
    }
}
