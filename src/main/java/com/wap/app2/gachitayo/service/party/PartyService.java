package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.Gender;
import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.Enum.PartyMemberRole;
import com.wap.app2.gachitayo.Enum.RequestGenderOption;
import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
import com.wap.app2.gachitayo.dto.response.PartyCreateResponseDto;
import com.wap.app2.gachitayo.dto.response.PartyResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.mapper.StopoverMapper;
import com.wap.app2.gachitayo.repository.party.PartyRepository;
import com.wap.app2.gachitayo.service.auth.GoogleAuthService;
import com.wap.app2.gachitayo.service.fare.PaymentStatusService;
import com.wap.app2.gachitayo.service.location.StopoverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;
    private final StopoverService stopoverService;
    private final StopoverMapper stopoverMapper;
    private final GoogleAuthService googleAuthService;
    private final PartyMemberService partyMemberService;
    private final PaymentStatusService paymentStatusService;

    @Transactional
    public ResponseEntity<PartyCreateResponseDto> createParty(String email, PartyCreateRequestDto requestDto) {
        log.info("\n=====파티 생성 시도=====");
        Member hostMember = googleAuthService.getUserByEmail(email);
        if (hostMember == null) throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);

        Stopover startStopover = stopoverService.createStopover(requestDto.getStartLocation().getLocation(), requestDto.getStartLocation().getStopoverType());
        Stopover destStopover = stopoverService.createStopover(requestDto.getDestination().getLocation(), requestDto.getDestination().getStopoverType());

        GenderOption genderOption = matchGenderOption(requestDto.getGenderOption(), hostMember);

        Party partyEntity = Party.builder()
                .stopovers(List.of(startStopover, destStopover))
                .maxPerson(requestDto.getMaxPerson())
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
                        .maxPeople(party.getMaxPerson())
                        .genderOption(party.getGenderOption())
                        .build()))
                .orElseGet(() -> notFoundPartyResponseEntity(partyId));
    }

    @Transactional
    public ResponseEntity<?> addStopoverToParty(Long partyId, StopoverDto requestDto) {
        Party partyEntity = partyRepository.findById(partyId).orElse(null);
        if(partyEntity == null) {
            return notFoundPartyResponseEntity(partyId);
        }

        Stopover stopoverEntity = stopoverService.createStopover(requestDto.getLocation(), requestDto.getStopoverType());

        boolean isExist = partyEntity.getStopovers().stream()
                .anyMatch(stopover -> stopover.getLocation().equals(stopoverEntity.getLocation()));

        if(!isExist) {
            stopoverService.setStopoverToParty(stopoverEntity, partyEntity);
            partyEntity.getStopovers().add(stopoverEntity);
            partyRepository.save(partyEntity);
        }

        return ResponseEntity.noContent().build();
    }

    @Transactional
    public ResponseEntity<?> updateStopover(Long partyId, StopoverDto stopoverDto) {
        if(stopoverDto.getId() == null) {
            return ResponseEntity.badRequest().body("missing target stopover id field in request body");
        }

        Party partyEntity = partyRepository.findById(partyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "party not found. please request again with existing party id"));

        Stopover stopoverEntity = partyEntity.getStopovers().stream()
                .filter(stopover -> stopover.getId().equals(stopoverDto.getId()))
                .findFirst().orElse(null);

        if(stopoverEntity == null) {
            return ResponseEntity.badRequest().body("cannot found target stopover with id: " + stopoverDto.getId());
        }

        return stopoverService.updateStopover(stopoverEntity, stopoverDto.getLocation(), stopoverDto.getStopoverType());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> searchPartiesWithDestinationLocation(String email, PartySearchRequestDto requestDto) {
        List<Party> parties = partyRepository.findPartiesWithRadius(requestDto.getLatitude(), requestDto.getLongitude(), requestDto.getRadius());
        Member member = googleAuthService.getUserByEmail(email);
        return ResponseEntity.ok(toPartyResponseDtoList(parties, member));
    }

    private List<PartyResponseDto> toPartyResponseDtoList(List<Party> parties, Member member) {
        List<PartyResponseDto> responseDtos = new ArrayList<>();
        for(Party party : parties) {
            int currentPeople = party.getPartyMemberList().size();
            String memberGender = member.getGender().name();
            String partyOption = party.getGenderOption().name().substring(5);
            if(currentPeople != party.getMaxPerson()
                && !partyMemberService.isInParty(party, member)
                && (party.getGenderOption().equals(GenderOption.MIXED) || memberGender.equals(partyOption))
            ) {
                responseDtos.add(
                        PartyResponseDto.builder()
                                .id(party.getId())
                                .members(partyMemberService.getPartyMemberResponseDtoList(party))
                                .stopovers(toStopoverDto(party))
                                .radius(party.getAllowRadius())
                                .maxPeople(party.getMaxPerson())
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
                .maxPerson(partyEntity.getMaxPerson())
                .radius(partyEntity.getAllowRadius())
                .genderOption(partyEntity.getGenderOption())
                .build();
    }
}
