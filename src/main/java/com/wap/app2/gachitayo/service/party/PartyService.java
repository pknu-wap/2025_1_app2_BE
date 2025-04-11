package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.*;
import com.wap.app2.gachitayo.domain.fare.Fare;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import com.wap.app2.gachitayo.dto.datadto.PartyMemberDto;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverAddRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverUpdateDto;
import com.wap.app2.gachitayo.dto.response.*;
import com.wap.app2.gachitayo.mapper.LocationMapper;
import com.wap.app2.gachitayo.mapper.StopoverMapper;
import com.wap.app2.gachitayo.repository.party.PartyRepository;
import com.wap.app2.gachitayo.service.fare.PaymentStatusService;
import com.wap.app2.gachitayo.service.location.StopoverService;
import com.wap.app2.gachitayo.service.member.MemberService;
import com.wap.app2.gachitayo.service.member.PartyMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;
    private final StopoverService stopoverService;
    private final StopoverMapper stopoverMapper;
    private final MemberService memberService;
    private final PartyMemberService partyMemberService;
    private final PaymentStatusService paymentStatusService;
    private final LocationMapper locationMapper;

    @Transactional
    public ResponseEntity<PartyCreateResponseDto> createParty(Long memberId, PartyCreateRequestDto requestDto) {
        log.info("=====파티 생성 시도=====");
        Member member = memberService.findMemberEntityById(memberId);
        if(member == null) {
            log.warn("존재하지 않는 유저 ID 요청");
            return ResponseEntity.badRequest().build();
        }

        Stopover startStopover = stopoverService.createStopover(requestDto.getStartLocation().getLocation(), requestDto.getStartLocation().getStopoverType());
        Stopover destStopover = stopoverService.createStopover(requestDto.getDestination().getLocation(), requestDto.getDestination().getStopoverType());

        GenderOption genderOption = matchGenderOption(requestDto, member);

        Party partyEntity = Party.builder()
                .stopovers(List.of(startStopover, destStopover))
                .maxPeople(requestDto.getMaxPerson())
                .allowRadius(requestDto.getRadius())
                .genderOption(genderOption)
                .build();

        startStopover.setParty(partyEntity);
        destStopover.setParty(partyEntity);

        PartyMember partyMemberEntity = partyMemberService.saveWithPartyAndMember(partyEntity, member, MemberRole.HOST);

        PaymentStatus paymentStatus = paymentStatusService.linkWithPartyMemberAndStopover(partyMemberEntity, destStopover);
        stopoverService.setPaymentStatus(destStopover, paymentStatus);
        partyRepository.save(partyEntity);

        log.info("=====파티 생성 성공=====");
        return ResponseEntity.ok(toResponseDto(partyEntity, member, startStopover, destStopover));
    }

    // test 용
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPartyInformationById(Long partyId) {
        Party partyEntity = partyRepository.findById(partyId).orElse(null);
        if(partyEntity == null) {
            return ResponseEntity.notFound().build();
        }

        List<PartyMemberResponseDto> partyMemberResponseDtos = partyMemberService.getPartyMembersResponseDto(partyEntity);
        return ResponseEntity.ok().body(PartyResponseDto.builder()
                .id(partyEntity.getId())
                .members(partyMemberResponseDtos)
                .stopovers(toStopoverDto(partyEntity))
                .radius(partyEntity.getAllowRadius())
                .maxPeople(partyEntity.getMaxPeople())
                .currentPeople(partyMemberResponseDtos.size())
                .genderOption(partyEntity.getGenderOption())
                .build());
    }

    @Transactional
    public ResponseEntity<?> attendParty(Long memberId, Long partyId) {
        log.info("=====유저가 파티에 참가 시도=====");
        Party partyEntity = partyRepository.findById(partyId).orElseThrow(() -> {
            log.warn("존재하지 않는 파티 ID 요청");
            return new ResponseStatusException(HttpStatus.BAD_REQUEST);});
        Member member = memberService.findMemberEntityById(memberId);

        if (member == null) {
            log.warn("존재하지 않는 유저 ID 요청");
            return ResponseEntity.badRequest().body("not found member with id: " + memberId);
        }

        if(partyEntity.getPartyMembers().size() == partyEntity.getMaxPeople()) {
            log.warn("파티 인원 초과");
            return ResponseEntity.badRequest().body("party is full");
        }

        PartyMember existingMember = partyMemberService.getPartyMemberByPartyAndMember(partyEntity, member);
        if (existingMember != null) {
            log.warn("이미 파티 내 존재하는 유저");
            return ResponseEntity.badRequest().body("already party member");
        }

        String memberGender = member.getGender().name();
        String partyOption = partyEntity.getGenderOption().name().substring(5).toUpperCase();
        if (!partyOption.equals(memberGender)) {
            log.warn("파티 성별 옵션에 맞지 않는 유저");
            return ResponseEntity.badRequest().body("forbidden to attend to party: gender not match with party option");
        }

        partyMemberService.saveWithPartyAndMember(partyEntity, member, MemberRole.MEMBER);
        log.info("=====참가 성공=====");
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public ResponseEntity<?> addStopoverToParty(Long partyId, Long hostId, StopoverAddRequestDto requestDto) {
        log.info("=====파티 내 하차 지점 추가 시도=====");
        Party partyEntity = partyRepository.findById(partyId).orElse(null);
        if(partyEntity == null) {
            log.warn("존재하지 않는 파티 ID 요청");
            return ResponseEntity.badRequest().body("not found party with id: " + partyId);
        }

        Member memberEntity = memberService.findMemberEntityById(requestDto.getMemberId());
        if(memberEntity == null) {
            log.warn("존재하지 않는 유저 ID 요청");
            return ResponseEntity.badRequest().body("not found member with id: " + requestDto.getMemberId());
        }

        PartyMember hostMember = partyEntity.getPartyMembers().stream().filter(member -> hostId.equals(member.getMember().getId())).findFirst().orElse(null);
        if(hostMember == null) {
            log.warn("해당 파티 내 존재하지 않는 방장 ID 요청");
            return ResponseEntity.badRequest().body("not found host with id: " + hostId);
        }

        if(hostMember.getRole() != MemberRole.HOST) {
            log.warn("방장이 아닌 유저의 요청");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not host member with id: " + hostId);
        }

        Stopover stopoverEntity = partyEntity.getStopovers().stream()
                .filter(stopover -> stopoverService.existWithLocation(stopover, locationMapper.toEntity(requestDto.getLocation()))).findFirst().orElse(null);

        if(stopoverEntity == null) {
            log.info("새로운 하차 지점 생성");
            stopoverEntity = stopoverService.createStopover(requestDto.getLocation(), LocationType.STOPOVER);
            stopoverEntity.setParty(partyEntity);
            partyEntity.getStopovers().add(stopoverEntity);
        }

        PartyMember partyMemberEntity = partyMemberService.getPartyMemberByPartyAndMember(partyEntity, memberEntity);
        PaymentStatus paymentStatus = paymentStatusService.linkWithPartyMemberAndStopover(partyMemberEntity, stopoverEntity);
        stopoverService.setPaymentStatus(stopoverEntity, paymentStatus);
        partyRepository.save(partyEntity);

        log.info("=====하차 지점 추가 성공=====");
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public ResponseEntity<?> updateStopover(Long partyId, StopoverUpdateDto requestDto) {
        log.info("=====하차 지점 수정 시도=====");
        if(requestDto.getStopoverId() == null) {
            log.warn("수정할 하차 지점 ID 찾을 수 없음");
            return ResponseEntity.badRequest().body("missing target stopover id field in request body");
        }

        Party partyEntity = partyRepository.findById(partyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "party not found. please request again with existing party id"));

        Stopover stopoverEntity = partyEntity.getStopovers().stream()
                .filter(stopover -> stopover.getId().equals(requestDto.getStopoverId()))
                .findFirst().orElse(null);

        if(stopoverEntity == null) {
            log.warn("존재하지 않는 하차 지점 ID 요청");
            return ResponseEntity.badRequest().body("cannot found target stopover with id: " + requestDto.getStopoverId());
        }

        boolean isUpdatedPaymentStatus = false;
        if(requestDto.getMemberId() != null) {
            Member memberEntity = memberService.findMemberEntityById(requestDto.getMemberId());
            if(memberEntity == null) {
                log.warn("존재하지 않는 유저 ID 요청");
                return ResponseEntity.badRequest().body("not found member with id: " + requestDto.getMemberId());
            }
            PartyMember partyMemberEntity = partyMemberService.getPartyMemberByPartyAndMember(partyEntity, memberEntity);
            isUpdatedPaymentStatus = paymentStatusService.updateStopover(partyMemberEntity, stopoverEntity);
        }

        boolean isUpdatedStopover = stopoverService.updateStopover(stopoverEntity, requestDto.getLocation(), requestDto.getStopoverType());
        if(isUpdatedPaymentStatus || isUpdatedStopover) {
            log.info("=====수정 사항 성공적으로 반영=====");
            return ResponseEntity.ok().body("updated stopover");
        }

        log.info("=====수정 사항 없음=====");
        return ResponseEntity.ok().body("nothing to update");
    }

    // 실제 서비스에서 조회용
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchPartyWithDestinationLocation(Long memberId, PartySearchRequestDto requestDto) {
        List<Party> parties = partyRepository.findPartiesWithRadius(requestDto.getLatitude(), requestDto.getLongitude(), requestDto.getRadius());
        Member memberEntity = memberService.findMemberEntityById(memberId);
        List<PartyResponseDto> responseDtos = toPartyResponseDtoList(parties, memberEntity);
        return ResponseEntity.ok(responseDtos);
    }

    // 금액 조회 테스트용
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPaymentStatusAndFare(Long partyId) {
        Party partyEntity = partyRepository.findById(partyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing party id"));

        List<PaymentStatusResponseDto> paymentStatusResponseDtoList = partyEntity.getPartyMembers().stream()
                .map(pm -> {
                    PaymentStatus paymentStatus = pm.getPaymentStatusList().stream()
                            .filter(pms -> pms.getPartyMember().equals(pm))
                            .findFirst().orElse(null);
                    return PaymentStatusResponseDto.builder()
                            .member(PartyMemberDto.builder()
                                    .member(MemberResponseDto.builder()
                                            .id(pm.getMember().getId())
                                            .name(pm.getMember().getName())
                                            .email(pm.getMember().getEmail())
                                            .phone(pm.getMember().getPhone())
                                            .profileImage(pm.getMember().getProfileImage())
                                            .gender(pm.getMember().getGender())
                                            .build())
                                    .memberRole(pm.getRole())
                                    .build())
                            .stopoverDto(StopoverDto.builder()
                                    .id(Optional.ofNullable(paymentStatus)
                                                    .map(PaymentStatus::getStopover)
                                                    .map(Stopover::getId)
                                                    .orElseThrow(() -> new IllegalStateException("Stopover not found")))
                                    .location(LocationDto.builder()
                                            .address(paymentStatus.getStopover().getLocation().getAddress())
                                            .latitude(paymentStatus.getStopover().getLocation().getLatitude())
                                            .longitude(paymentStatus.getStopover().getLocation().getLongitude())
                                            .build())
                                    .stopoverType(paymentStatus.getStopover().getStopoverType())
                                    .build())
                            .fareToPay(
                                    Optional.of(paymentStatus)
                                            .map(PaymentStatus::getStopover)
                                            .map(Stopover::getFare)
                                            .map(Fare::getFinalFigure)
                                    .orElseThrow(() -> new IllegalStateException("Fare not found"))
                            )
                            .isPaid(paymentStatus.isPaid())
                            .build();
                }).collect(Collectors.toList());

        return ResponseEntity.ok().body(PartyFareAndPaymentStatusResponseDto.builder()
                .partyId(partyEntity.getId())
                .paymentStatus(paymentStatusResponseDtoList)
                .build());
    }

    // 금액 조회 테스트용 쿼리 개선 버전
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPaymentStatusAdvanced(Long partyId) {
        List<PartyMember> partyMemberList = partyMemberService.getPartyMemberDetailsWithPartyId(partyId);

        List<PaymentStatusResponseDto> responseDtoList = partyMemberList.stream()
                .map(pm -> {
                    PaymentStatus paymentStatus = pm.getPaymentStatusList().stream()
                            .filter(ps -> ps.getPartyMember().equals(pm))
                            .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot found payment status with party member id " + pm.getId()));

                    Stopover stopover = Optional.ofNullable(paymentStatus.getStopover())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot found stopover with payment status id " + paymentStatus.getId()));

                    Fare fare = Optional.ofNullable(stopover.getFare())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "cannot found fare with stopover id " + stopover.getId()));

                    return PaymentStatusResponseDto.builder()
                            .member(PartyMemberDto.builder()
                                    .member(MemberResponseDto.builder()
                                            .id(pm.getMember().getId())
                                            .name(pm.getMember().getName())
                                            .email(pm.getMember().getEmail())
                                            .phone(pm.getMember().getPhone())
                                            .profileImage(pm.getMember().getProfileImage())
                                            .gender(pm.getMember().getGender())
                                            .build())
                                    .memberRole(pm.getRole())
                                    .build())
                            .stopoverDto(StopoverDto.builder()
                                    .id(stopover.getId())
                                    .location(LocationDto.builder()
                                            .address(stopover.getLocation().getAddress())
                                            .latitude(stopover.getLocation().getLatitude())
                                            .longitude(stopover.getLocation().getLongitude())
                                            .build())
                                    .build())
                            .isPaid(paymentStatus.isPaid())
                            .fareToPay(fare.getFinalFigure())
                            .build();
                }).toList();
        return ResponseEntity.ok().body(responseDtoList);
    }

    private List<PartyResponseDto> toPartyResponseDtoList(List<Party> parties, Member memberEntity) {
        List<PartyResponseDto> responseDtos = new ArrayList<>();
        for(Party party : parties) {
            int currentPeople = party.getPartyMembers().size();
            String memberGender = memberEntity.getGender().name();
            String partyOption = party.getGenderOption().name().substring(5);
            if (currentPeople != party.getMaxPeople()
                    && !partyMemberService.isInParty(party, memberEntity)
                    && (party.getGenderOption().equals(GenderOption.MIXED) || memberGender.equals(partyOption))
            ) {
                responseDtos.add(
                        PartyResponseDto.builder()
                                .id(party.getId())
                                .members(partyMemberService.getPartyMembersResponseDto(party))
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

    private GenderOption matchGenderOption(PartyCreateRequestDto requestDto, Member member) {
        GenderOption genderOption = (requestDto.getGenderOption().equals(RequestGenderOption.ONLY))? null : GenderOption.MIXED;
        if(genderOption == null) {
            Gender gender = member.getGender();
            if(gender == Gender.MALE) {
                genderOption = GenderOption.ONLY_MALE;
            } else {
                genderOption = GenderOption.ONLY_FEMALE;
            }
        }
        return genderOption;
    }

    private List<StopoverDto> toStopoverDto(Party party) {
        return party.getStopovers().stream()
                .map(stopoverMapper::toDto)
                .collect(Collectors.toList());
    }

    private PartyCreateResponseDto toResponseDto(Party partyEntity, Member member, Stopover startStopover, Stopover destStopover) {
        return PartyCreateResponseDto.builder()
                .id(partyEntity.getId())
                .hostId(member.getId())
                .startLocation(stopoverMapper.toDto(startStopover))
                .destination(stopoverMapper.toDto(destStopover))
                .maxPerson(partyEntity.getMaxPeople())
                .radius(partyEntity.getAllowRadius())
                .genderOption(partyEntity.getGenderOption())
                .build();
    }
}
