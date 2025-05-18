package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.AdditionalRole;
import com.wap.app2.gachitayo.Enum.PartyMemberRole;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.response.PartyMemberResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
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
        PartyMember partyMember = PartyMember.builder()
                .party(party)
                .member(member)
                .memberRole(role)
                .build();

        party.getPartyMemberList().add(partyMember);
        return partyMemberRepository.save(partyMember);
    }

    @Transactional(readOnly = true)
    public boolean isInParty(Party party, Member member) {
        return partyMemberRepository.existsByPartyAndMember(party, member);
    }

    @Transactional(readOnly = true)
    public PartyMember getPartyMemberByPartyAndMember(Party party, Member member) {
        return partyMemberRepository.findByPartyAndMember(party, member).orElseThrow(() -> new TagogayoException(ErrorCode.NOT_IN_PARTY));
    }

    @Transactional(readOnly = true)
    public List<PartyMemberResponseDto> getPartyMemberResponseDtoList(Party party) {
        List<PartyMember> partyMemberList = partyMemberRepository.findAllByParty(party);
        return partyMemberList.stream().map(pm ->
                toResponseDto(pm.getMember(), pm.getMemberRole())).toList();
    }

    @Transactional(readOnly = true)
    public PartyMember getPartyMemberById(Long id) {
        return partyMemberRepository.findById(id).orElseThrow(() -> new TagogayoException(ErrorCode.NOT_IN_PARTY));
    }

    @Transactional
    public void changePartyMemberRole(PartyMember partyMember, PartyMemberRole role) {
        partyMember.setMemberRole(role);
        partyMemberRepository.save(partyMember);
    }

    @Transactional
    public void changeAdditionalRole(PartyMember partyMember, AdditionalRole role) {
        partyMember.setAdditionalRole(role);
        partyMemberRepository.save(partyMember);
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
