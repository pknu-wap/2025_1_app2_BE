package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.response.PartyCreateResponseDto;
import com.wap.app2.gachitayo.dto.response.PartyResponseDto;
import com.wap.app2.gachitayo.mapper.StopoverMapper;
import com.wap.app2.gachitayo.repository.party.PartyRepository;
import com.wap.app2.gachitayo.service.location.LocationService;
import com.wap.app2.gachitayo.service.location.StopoverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;
    private final LocationService locationService;
    private final StopoverService stopoverService;
    private final StopoverMapper stopoverMapper;

    @Transactional
    public ResponseEntity<PartyCreateResponseDto> createParty(PartyCreateRequestDto requestDto) {
        Location startLocation = locationService.createOrGetLocation(requestDto.getStartLocation().getLocation());
        Stopover startStopover = stopoverService.findOrCreateStopover(startLocation, requestDto.getStartLocation().getStopoverType());
        locationService.updateStopover(startLocation, startStopover);

        Location destLocation = locationService.createOrGetLocation(requestDto.getDestination().getLocation());
        Stopover destStopover = stopoverService.findOrCreateStopover(destLocation, requestDto.getDestination().getStopoverType());
        locationService.updateStopover(destLocation, destStopover);

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
                        .stopovers(toStopoverResponseList(party))
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

        Location locationEntity = locationService.createOrGetLocation(requestDto.getLocation());
        Stopover stopoverEntity = stopoverService.findOrCreateStopover(locationEntity, requestDto.getStopoverType());
        locationService.updateStopover(locationEntity, stopoverEntity);

        boolean isExist = partyEntity.getStopovers().stream()
                .anyMatch(stopover -> stopover.getLocation().equals(stopoverEntity.getLocation()));

        if(!isExist) {
            stopoverService.setStopoverToParty(stopoverEntity, partyEntity);
            partyEntity.getStopovers().add(stopoverEntity);
            partyRepository.save(partyEntity);
        }

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<PartyResponseDto> notFoundPartyResponseEntity(Long partyId) {
        log.error("Party not found with id: {}", partyId);
        return ResponseEntity.notFound().build();
    }

    private List<StopoverResponseDto> toStopoverResponseList(Party party) {
        return party.getStopovers().stream()
                .map(stopover -> new StopoverResponseDto(party.getId(), stopoverMapper.toDto(stopover)))
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
