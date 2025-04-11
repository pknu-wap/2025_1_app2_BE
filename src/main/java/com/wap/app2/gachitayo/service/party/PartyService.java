package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
import com.wap.app2.gachitayo.dto.response.PartyCreateResponseDto;
import com.wap.app2.gachitayo.dto.response.PartyResponseDto;
import com.wap.app2.gachitayo.mapper.StopoverMapper;
import com.wap.app2.gachitayo.repository.party.PartyRepository;
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

    @Transactional
    public ResponseEntity<PartyCreateResponseDto> createParty(String email, PartyCreateRequestDto requestDto) {
        Stopover startStopover = stopoverService.createStopover(requestDto.getStartLocation().getLocation(), requestDto.getStartLocation().getStopoverType());
        Stopover destStopover = stopoverService.createStopover(requestDto.getDestination().getLocation(), requestDto.getDestination().getStopoverType());

        Party partyEntity = Party.builder()
                .stopovers(List.of(startStopover, destStopover))
                .maxPerson(requestDto.getMaxPerson())
                .allowRadius(requestDto.getRadius())
                .genderOption(requestDto.getGenderOption())
                .build();

        stopoverService.setStopoverToParty(startStopover, partyEntity);
        stopoverService.setStopoverToParty(destStopover, partyEntity);
        partyRepository.save(partyEntity);

        return ResponseEntity.ok(toResponseDto(partyEntity, startStopover, destStopover));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getPartyInformationById(Long partyId) {
        return partyRepository.findById(partyId)
                .map(party -> ResponseEntity.ok(PartyResponseDto.builder()
                        .id(party.getId())
                        .stopovers(toStopoverDto(party))
                        .radius(party.getAllowRadius())
                        .maxPerson(party.getMaxPerson())
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
    public ResponseEntity<?> searchPartyWithDestinationLocation(PartySearchRequestDto requestDto) {
        List<Party> parties = partyRepository.findPartiesWithRadius(requestDto.getLatitude(), requestDto.getLongitude(), requestDto.getRadius());
        List<PartyResponseDto> responseDtos = new ArrayList<>();
        for(Party party : parties) {
            responseDtos.add(
                    PartyResponseDto.builder()
                            .id(party.getId())
                            .stopovers(toStopoverDto(party))
                            .radius(party.getAllowRadius())
                            .maxPerson(party.getMaxPerson())
                            .genderOption(party.getGenderOption())
                            .build()
            );
        }
        return ResponseEntity.ok(responseDtos);
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

    private PartyCreateResponseDto toResponseDto(Party partyEntity, Stopover startStopover, Stopover destStopover) {
        return PartyCreateResponseDto.builder()
                .id(partyEntity.getId())
                .startLocation(stopoverMapper.toDto(startStopover))
                .destination(stopoverMapper.toDto(destStopover))
                .maxPerson(partyEntity.getMaxPerson())
                .radius(partyEntity.getAllowRadius())
                .genderOption(partyEntity.getGenderOption())
                .build();
    }
}
