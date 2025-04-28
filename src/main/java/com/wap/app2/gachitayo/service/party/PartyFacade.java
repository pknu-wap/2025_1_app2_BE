package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.*;
import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.response.PartyCreateResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.mapper.StopoverMapper;
import com.wap.app2.gachitayo.service.fare.PaymentStatusService;
import com.wap.app2.gachitayo.service.location.StopoverService;
import com.wap.app2.gachitayo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyFacade {
    private final PartyService partyService;
    private final StopoverService stopoverService;
    private final PaymentStatusService paymentStatusService;
    private final MemberService memberService;
    private final PartyMemberService partyMemberService;

    private final StopoverMapper stopoverMapper;

    public ResponseEntity<?> createParty(String email, PartyCreateRequestDto requestDto) {
        // 1. 사용자 조회
        Member host = memberService.getUserByEmail(email);
        if(host == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        // 2. 출발/목적지 생성
        Stopover start = stopoverService.createStopover(requestDto.getStartLocation().getLocation(), LocationType.START);
        Stopover dest = stopoverService.createStopover(requestDto.getDestination().getLocation(), LocationType.DESTINATION);

        // 3. 파티 생성
        Party party = partyService.createParty(
                List.of(start, dest),
                requestDto.getMaxPerson(),
                requestDto.getRadius(),
                resolveGenderOption(requestDto.getGenderOption(), host)
        );

        // 4. Stopover - Party 연결
        start.setParty(party);
        dest.setParty(party);

        // 5. PartyMember 생성
        PartyMember hostMember = partyMemberService.connectMemberWithParty(party, host, PartyMemberRole.HOST);

        // 6. 목적지 - PaymentStatus 연결
        PaymentStatus paymentStatus = paymentStatusService.connectPartyMemberWithStopover(hostMember, dest);
        stopoverService.addPaymentStatus(dest, paymentStatus);

        return ResponseEntity.ok().body(toResponseDto(party, host, start, dest));
    }

    private PartyCreateResponseDto toResponseDto(Party partyEntity, Member member, Stopover startStopover, Stopover destStopover) {
        return PartyCreateResponseDto.builder()
                .id(partyEntity.getId())
                .hostName(member.getName())
                .hostEmail(member.getEmail())
                .startLocation(stopoverMapper.toDto(startStopover))
                .destination(stopoverMapper.toDto(destStopover))
                .maxPeople(partyEntity.getMaxPeople())
                .radius(partyEntity.getAllowRadius())
                .genderOption(partyEntity.getGenderOption())
                .build();
    }

    public ResponseEntity<?> attendParty(String email, Long partyId) {
        // 1. Party 조회
        Party party = partyService.findPartyById(partyId);

        // 2. Member 조회
        Member member = memberService.getUserByEmail(email);
        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        // 3. Validation
        if (party.getPartyMemberList().size() >= party.getMaxPeople()) {
            throw new TagogayoException(ErrorCode.EXCEED_PARTY_MEMBER);
        }
        if (partyMemberService.isInParty(party, member)) {
            throw new TagogayoException(ErrorCode.ALREADY_PARTY_MEMBER);
        }
        validateGenderOption(party.getGenderOption(), member.getGender());

        // 4. PartyMember 등록
        PartyMember participant = partyMemberService.connectMemberWithParty(party, member, PartyMemberRole.MEMBER);

        // 5. Map 으로 간단히 응답
        return ResponseEntity.ok(Map.of(
                "message", "파티 참가 요청이 완료되었습니다.",
                "party_id", party.getId(),
                "party_member_id", participant.getId(),
                "party_members", party.getPartyMemberList()
        ));
    }
    private void validateGenderOption(GenderOption genderOption, Gender memberGender) {
        if (genderOption == GenderOption.MIXED) return;
        String optionGender = genderOption.name().substring(5); // "ONLY_MALE" → "MALE"
        if (!optionGender.equalsIgnoreCase(memberGender.name())) {
            throw new TagogayoException(ErrorCode.NOT_MATCH_GENDER_OPTION);
        }
    }

    private GenderOption resolveGenderOption(RequestGenderOption requestGenderOption, Member host) {
        if(requestGenderOption == RequestGenderOption.ONLY){
            if(host.getGender() == Gender.MALE) return GenderOption.ONLY_MALE;
            else return GenderOption.ONLY_FEMALE;
        }
        return GenderOption.MIXED;
    }
}
