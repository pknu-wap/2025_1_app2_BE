package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.PartyMemberRole;
import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.response.PartyMemberResponseDto;
import com.wap.app2.gachitayo.repository.party.PartyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public boolean isInParty(Party party, Member member) {
        return partyMemberRepository.existsByPartyAndMember(party, member);
    }

    @Transactional(readOnly = true)
    public List<PartyMemberResponseDto> getPartyMemberResponseDtoList(Party party) {
        List<PartyMember> partyMemberList = partyMemberRepository.findAllByParty(party);
        return partyMemberList.stream().map(pm ->
                toResponseDto(pm.getMember(), pm.getMemberRole())).toList();
    }

    private PartyMemberResponseDto toResponseDto(Member member, PartyMemberRole role) {
        return PartyMemberResponseDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .gender(member.getGender())
                .role(role)
                .build();
    }
}
