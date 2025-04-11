package com.wap.app2.gachitayo.service.member;

import com.wap.app2.gachitayo.Enum.MemberRole;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.response.PartyMemberResponseDto;
import com.wap.app2.gachitayo.repository.member.PartyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyMemberService {
    private final PartyMemberRepository partyMemberRepository;

    @Transactional
    public PartyMember saveWithPartyAndMember(Party party, Member member, MemberRole role) {
        PartyMember partyMember = PartyMember.builder()
                .party(party)
                .member(member)
                .role(role)
                .build();
        return partyMemberRepository.save(partyMember);
    }

    @Transactional(readOnly = true)
    public PartyMember getPartyMemberByPartyAndMember(Party party, Member member) {
        return partyMemberRepository.findByPartyAndMember(party, member);
    }

    @Transactional(readOnly = true)
    public List<PartyMemberResponseDto> getPartyMembersResponseDto(Party party) {
        List<PartyMember> partyMembers = partyMemberRepository.findAllByParty(party);
        return partyMembers.stream().map(partyMember ->
            toResponseDto(partyMember.getMember(), partyMember.getRole())).toList();
    }

    @Transactional(readOnly = true)
    public boolean isInParty(Party party, Member member) {
        return partyMemberRepository.existsByPartyAndMember(party, member);
    }

    @Transactional(readOnly = true)
    public List<PartyMember> getPartyMemberDetailsWithPartyId(Long partyId) {
        return partyMemberRepository.findWithAllDetailsByPartyId(partyId);
    }

    private PartyMemberResponseDto toResponseDto(Member member, MemberRole role) {
        return PartyMemberResponseDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .gender(member.getGender())
                .role(role)
                .build();
    }
}
