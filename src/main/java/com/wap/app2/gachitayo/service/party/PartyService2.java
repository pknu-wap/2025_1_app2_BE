package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.*;
import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverAddRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverUpdateDto;
import com.wap.app2.gachitayo.dto.response.PartyCreateResponseDto;
import com.wap.app2.gachitayo.dto.response.PartyResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.mapper.LocationMapper;
import com.wap.app2.gachitayo.mapper.StopoverMapper;
import com.wap.app2.gachitayo.repository.party.PartyRepository;
import com.wap.app2.gachitayo.service.fare.PaymentStatusService;
import com.wap.app2.gachitayo.service.location.StopoverService;
import com.wap.app2.gachitayo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyService2 {
    private final PartyRepository partyRepository;
    private final StopoverService stopoverService;
    private final StopoverMapper stopoverMapper;
    private final MemberService memberService;
    private final PartyMemberService partyMemberService;
    private final PaymentStatusService paymentStatusService;
    private final LocationMapper locationMapper;

    @Transactional
    public ResponseEntity<PartyCreateResponseDto> createParty(String email, PartyCreateRequestDto requestDto) {
        log.info("\n=====파티 생성 시도=====");
        Member hostMember = memberService.getUserByEmail(email);
        if (hostMember == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        Stopover startStopover = stopoverService.createStopover(requestDto.getStartLocation().getLocation(), requestDto.getStartLocation().getStopoverType());
        Stopover destStopover = stopoverService.createStopover(requestDto.getDestination().getLocation(), requestDto.getDestination().getStopoverType());

        GenderOption genderOption = matchGenderOption(requestDto.getGenderOption(), hostMember);

        Party partyEntity = Party.builder()
                .stopovers(List.of(startStopover, destStopover))
                .maxPeople(requestDto.getMaxPerson())
                .allowRadius(requestDto.getRadius())
                .genderOption(genderOption)
                .build();

        startStopover.setParty(partyEntity);
        destStopover.setParty(partyEntity);

        PartyMember partyMember = partyMemberService.connectMemberWithParty(partyEntity, hostMember, PartyMemberRole.HOST);

        PaymentStatus paymentStatus = paymentStatusService.connectPartyMemberWithStopover(partyMember, destStopover);
        stopoverService.addPaymentStatus(destStopover, paymentStatus);
        partyRepository.save(partyEntity);

        log.info("\n=====파티 생성 성공=====");
        return ResponseEntity.ok(toResponseDto(partyEntity, hostMember, startStopover, destStopover));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getPartyInformationById(Long partyId) {
        return partyRepository.findById(partyId)
                .map(party -> ResponseEntity.ok(PartyResponseDto.builder()
                        .id(party.getId())
                        .stopovers(toStopoverDto(party))
                        .radius(party.getAllowRadius())
                        .maxPeople(party.getMaxPeople())
                        .genderOption(party.getGenderOption())
                        .build()))
                .orElseGet(() -> notFoundPartyResponseEntity(partyId));
    }

    @Transactional
    public ResponseEntity<?> attendParty(String email, Long partyId) {
        log.info("\n=====유저가 파티 참가 시도=====");
        Party party = partyRepository.findById(partyId).orElseThrow(() -> new TagogayoException(ErrorCode.PARTY_NOT_FOUND));
        Member member = memberService.getUserByEmail(email);
        if (member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
        if(party.getPartyMemberList().size() == party.getMaxPeople()) throw new TagogayoException(ErrorCode.EXCEED_PARTY_MEMBER);
        if(partyMemberService.isInParty(party, member)) throw new TagogayoException(ErrorCode.ALREADY_PARTY_MEMBER);

        String memberGender = member.getGender().name();
        String partyOption = party.getGenderOption().name().substring(5);
        if(!partyOption.equalsIgnoreCase(memberGender)) throw new TagogayoException(ErrorCode.NOT_MATCH_GENDER_OPTION);

        PartyMember participant = partyMemberService.connectMemberWithParty(party, member, PartyMemberRole.MEMBER);
        log.info("\n=====참가 성공=====");
        // DTO 만들기 귀찮아서 Map 으로 응답합니다.
        Map<String, Object> responseMap = Map.of(
                "message", "파티 참가 요청이 완료되었습니다.",
                "party_id", party.getId(),
                "party_member_id", participant.getId()
        );
        return ResponseEntity.ok().body(responseMap);
    }

    @Transactional
    public ResponseEntity<?> addStopoverToParty(String email, Long partyId, StopoverAddRequestDto requestDto) {
        log.info("\n=====파티 내 하차 지점 추가 시도=====");
        Party partyEntity = partyRepository.findById(partyId).orElseThrow(() -> new TagogayoException(ErrorCode.PARTY_NOT_FOUND));

        // HOST 검증
        Member hostMember = memberService.getUserByEmail(email);
        if (hostMember == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
        PartyMember partyHost = partyEntity.getPartyMemberList().stream()
                .filter(pm -> hostMember.getId().equals(pm.getMember().getId())).findFirst()
                .orElseThrow(() -> new TagogayoException(ErrorCode.NOT_IN_PARTY));
        if(!partyHost.getMemberRole().equals(PartyMemberRole.HOST)) throw new TagogayoException(ErrorCode.NOT_HOST);

        // 연결 시킬 유저 검증
        Member participant = memberService.getUserByEmail(requestDto.getMemberEmail());
        if(participant == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
        if(!partyMemberService.isInParty(partyEntity, participant)) throw new TagogayoException(ErrorCode.NOT_IN_PARTY);

        Stopover stopoverEntity = partyEntity.getStopovers().stream()
                .filter(s -> existStopoverWithLocation(s, locationMapper.toEntity(requestDto.getLocation()))).findFirst().orElse(null);

        boolean isCreated = false;
        if(stopoverEntity == null) {
            log.info("새로운 하차 지점 생성");
            stopoverEntity = stopoverService.createStopover(requestDto.getLocation(), LocationType.STOPOVER);
            stopoverEntity.setParty(partyEntity);
            partyEntity.getStopovers().add(stopoverEntity);
            isCreated = true;
        }

        PartyMember partyMember = partyMemberService.getPartyMemberByPartyAndMember(partyEntity, participant);
        PaymentStatus paymentStatus = paymentStatusService.connectPartyMemberWithStopover(partyMember, stopoverEntity);
        stopoverService.addPaymentStatus(stopoverEntity, paymentStatus);
        partyRepository.save(partyEntity);


        log.info("\n=====하차 지점 추가 성공=====");
        if(isCreated) {
            return ResponseEntity.ok().body(Map.of(
                    "message", "새로운 하차 지점 생성",
                    "stopover", stopoverMapper.toDto(stopoverEntity)
            ));
        }
        return ResponseEntity.ok().body(Map.of(
                "message", "기존 하차 지점 사용",
                "stopover", stopoverMapper.toDto(stopoverEntity)
        ));
    }

    @Transactional
    public ResponseEntity<?> updateStopover(String email, Long partyId, StopoverUpdateDto updateDto) {
        Party partyEntity = partyRepository.findById(partyId)
                .orElseThrow(() -> new TagogayoException(ErrorCode.PARTY_NOT_FOUND));

        Member hostMember = memberService.getUserByEmail(email);
        if(hostMember == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
        PartyMember host = partyMemberService.getPartyMemberByPartyAndMember(partyEntity, hostMember);
        if(host == null) throw new TagogayoException(ErrorCode.NOT_IN_PARTY);
        if(!host.getMemberRole().equals(PartyMemberRole.HOST)) throw new TagogayoException(ErrorCode.NOT_HOST);

        Stopover stopoverEntity = partyEntity.getStopovers().stream()
                .filter(stopover -> stopover.getId().equals(updateDto.getStopoverId()))
                .findFirst().orElse(null);

        if(stopoverEntity == null) throw new TagogayoException(ErrorCode.STOPOVER_NOT_FOUND);

        boolean isUpdatedPayementStatus = false;
        if(updateDto.getMemberEmail() != null) {
            Member member = memberService.getUserByEmail(updateDto.getMemberEmail());
            if(member == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
            PartyMember partyMember = partyMemberService.getPartyMemberByPartyAndMember(partyEntity, member);
            if(partyMember == null) throw new TagogayoException(ErrorCode.NOT_IN_PARTY);
            isUpdatedPayementStatus = paymentStatusService.updateStopover(partyMember, stopoverEntity);
        }
        boolean isUpdatedStopover = stopoverService.updateStopover(stopoverEntity, updateDto.getLocation(), updateDto.getStopoverType());
        if(isUpdatedStopover || isUpdatedPayementStatus) {
            log.info("\n=====수정 사항 성공적으로 반영=====");
            return ResponseEntity.ok().body(Map.of(
               "stopover", stopoverMapper.toDto(stopoverEntity)
            ));
        }
        log.info("\n=====수정 사항 없음=====");
        return ResponseEntity.noContent().build();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> searchPartiesWithDestinationLocation(String email, PartySearchRequestDto requestDto) {
        List<Party> parties = partyRepository.findPartiesWithRadius(requestDto.getLatitude(), requestDto.getLongitude(), requestDto.getRadius());
        Member member = memberService.getUserByEmail(email);
        return ResponseEntity.ok(toPartyResponseDtoList(parties, member));
    }

    private List<PartyResponseDto> toPartyResponseDtoList(List<Party> parties, Member member) {
        List<PartyResponseDto> responseDtos = new ArrayList<>();
        for(Party party : parties) {
            int currentPeople = party.getPartyMemberList().size();
            String memberGender = member.getGender().name();
            String partyOption = party.getGenderOption().name().substring(5);
            if(currentPeople != party.getMaxPeople()
                && !partyMemberService.isInParty(party, member)
                && (party.getGenderOption().equals(GenderOption.MIXED) || memberGender.equals(partyOption))
            ) {
                responseDtos.add(
                        PartyResponseDto.builder()
                                .id(party.getId())
                                .members(partyMemberService.getPartyMemberResponseDtoList(party))
                                .stopovers(toStopoverDto(party))
                                .radius(party.getAllowRadius())
                                .maxPeople(party.getMaxPeople())
                                .currentPeople(currentPeople)
                                .genderOption(party.getGenderOption())
                                .build()
                );
            }
        }
        return responseDtos;
    }

    private GenderOption matchGenderOption(RequestGenderOption requestGenderOption, Member member) {
        GenderOption genderOption = (requestGenderOption.equals(RequestGenderOption.ONLY))? null : GenderOption.MIXED;
        if(genderOption == null) {
            Gender gender = member.getGender();
            if(gender.equals(Gender.MALE)) {
                genderOption = GenderOption.ONLY_MALE;
            } else {
                genderOption = GenderOption.ONLY_FEMALE;
            }
        }
        return genderOption;
    }

    private boolean existStopoverWithLocation(Stopover stopover, Location location) {
        return stopover.getLocation().getAddress().equalsIgnoreCase(location.getAddress())
                && stopover.getLocation().getLatitude() == location.getLatitude()
                && stopover.getLocation().getLongitude() == location.getLongitude();
    }

    private ResponseEntity<PartyResponseDto> notFoundPartyResponseEntity(Long partyId) {
        log.error("Party not found with id: {}", partyId);
        return ResponseEntity.notFound().build();
    }

    private List<StopoverDto> toStopoverDto(Party party) {
        return party.getStopovers().stream()
                .map(stopoverMapper::toDto)
                .collect(Collectors.toList());
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
}
