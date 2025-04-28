package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.*;
import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverAddRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverUpdateDto;
import com.wap.app2.gachitayo.dto.response.PartyCreateResponseDto;
import com.wap.app2.gachitayo.dto.response.PartyResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.mapper.StopoverMapper;
import com.wap.app2.gachitayo.service.fare.PaymentStatusService;
import com.wap.app2.gachitayo.service.location.StopoverService;
import com.wap.app2.gachitayo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public ResponseEntity<?> addStopoverToParty(String email, Long partyId, StopoverAddRequestDto requestDto) {
        log.info("\n===== 파티 내 하차 지점 추가 시도 =====");

        // 1. Party 조회
        Party party = partyService.findPartyById(partyId);

        // 2. HOST 검증 - MemberService 단에서 검증 후 리턴으로 리펙토링 가능
        Member hostMember = memberService.getUserByEmail(email);
        if (hostMember == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        // PartyMemberService 단에서 검증 후 리턴으로 리팩토링 가능
        PartyMember host = partyMemberService.getPartyMemberByPartyAndMember(party, hostMember);
        if (host == null) throw new TagogayoException(ErrorCode.NOT_IN_PARTY);
        if (!host.getMemberRole().equals(PartyMemberRole.HOST)) throw new TagogayoException(ErrorCode.NOT_HOST);

        // 3. 추가할 참가자 Member 검증
        Member participant = memberService.getUserByEmail(requestDto.getMemberEmail());
        if (participant == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        PartyMember participantMember = partyMemberService.getPartyMemberByPartyAndMember(party, participant);
        if (participantMember == null) throw new TagogayoException(ErrorCode.NOT_IN_PARTY);

        // 4. Stopover 찾거나 생성
        Stopover stopover = partyService.findOrCreateStopover(party, requestDto.getLocation());

        // 5. PaymentStatus 연결
        PaymentStatus paymentStatus = paymentStatusService.connectPartyMemberWithStopover(participantMember, stopover);
        stopoverService.addPaymentStatus(stopover, paymentStatus);

        log.info("\n===== 하차 지점 추가 성공 =====");

        boolean isNewStopover = stopover.getId() == null;
        return ResponseEntity.ok(Map.of(
                "message", isNewStopover ? "새로운 하차 지점 생성" : "기존 하차 지점 사용",
                "stopover", stopoverMapper.toDto(stopover)
        ));
    }

    public ResponseEntity<?> updateStopover(String email, Long partyId, StopoverUpdateDto updateDto) {
        log.info("\n===== 파티 내 하차 지점 수정 시도 =====");

        // 1. 파티 조회
        Party party = partyService.findPartyById(partyId);

        // 2. HOST 검증
        Member hostMember = memberService.getUserByEmail(email);
        if (hostMember == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        PartyMember host = partyMemberService.getPartyMemberByPartyAndMember(party, hostMember);
        if (host == null) throw new TagogayoException(ErrorCode.NOT_IN_PARTY);
        if (!host.getMemberRole().equals(PartyMemberRole.HOST)) throw new TagogayoException(ErrorCode.NOT_HOST);

        // 3. Stopover 조회
        Stopover stopover = party.getStopovers().stream()
                .filter(s -> s.getId().equals(updateDto.getStopoverId()))
                .findFirst()
                .orElseThrow(() -> new TagogayoException(ErrorCode.STOPOVER_NOT_FOUND));

        // 4. Stopover 수정
        boolean isUpdatedStopover = stopoverService.updateStopover(stopover, updateDto.getLocation(), updateDto.getStopoverType());

        // 5. PaymentStatus (참가자 변경 시) 수정
        boolean isUpdatedPaymentStatus = false;
        if (updateDto.getMemberEmail() != null) {
            Member member = memberService.getUserByEmail(updateDto.getMemberEmail());
            if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

            PartyMember partyMember = partyMemberService.getPartyMemberByPartyAndMember(party, member);
            if (partyMember == null) throw new TagogayoException(ErrorCode.NOT_IN_PARTY);

            isUpdatedPaymentStatus = paymentStatusService.updateStopover(partyMember, stopover);
        }

        // 6. 결과 반환
        if (isUpdatedStopover || isUpdatedPaymentStatus) {
            log.info("\n===== 수정 사항 성공 반영 =====");
            return ResponseEntity.ok(Map.of(
                    "stopovers", party.getStopovers().stream().map(stopoverMapper::toDto).toList()
            ));
        }

        log.info("\n===== 수정 사항 없음 =====");
        return ResponseEntity.noContent().build();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> searchPartiesWithDestinationLocation(String email, PartySearchRequestDto requestDto) {
        log.info("\n===== 위치 기반 파티 검색 시도 =====");

        // 1. 현재 사용자 조회
        Member member = memberService.getUserByEmail(email);
        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        // 2. 반경 내 파티 조회
        List<Party> parties = partyService.findPartiesWithinRadius(requestDto.getLatitude(), requestDto.getLongitude(), requestDto.getRadius());

        // 3. 필터링 (인원수, 성별옵션, 참가 여부)
        List<PartyResponseDto> partyDtos = parties.stream()
                .filter(party -> {
                    int currentPeople = party.getPartyMemberList().size();
                    String memberGender = member.getGender().name();
                    String partyOption = party.getGenderOption().name().substring(5);

                    return currentPeople != party.getMaxPeople()
                            && !partyMemberService.isInParty(party, member)
                            && (party.getGenderOption().equals(GenderOption.MIXED) || memberGender.equalsIgnoreCase(partyOption));
                })
                .map(party -> PartyResponseDto.builder()
                        .id(party.getId())
                        .members(partyMemberService.getPartyMemberResponseDtoList(party))
                        .stopovers(party.getStopovers().stream().map(stopoverMapper::toDto).toList())
                        .radius(party.getAllowRadius())
                        .maxPeople(party.getMaxPeople())
                        .currentPeople(party.getPartyMemberList().size())
                        .genderOption(party.getGenderOption())
                        .build()
                )
                .toList();

        return ResponseEntity.ok(partyDtos);
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
