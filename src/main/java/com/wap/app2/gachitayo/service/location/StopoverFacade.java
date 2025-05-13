package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.Enum.AdditionalRole;
import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.Enum.PartyMemberRole;
import com.wap.app2.gachitayo.domain.fare.Fare;
import com.wap.app2.gachitayo.domain.fare.PaymentStatus;
import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import com.wap.app2.gachitayo.dto.request.FareRequestDto;
import com.wap.app2.gachitayo.mapper.LocationMapper;
import com.wap.app2.gachitayo.service.fare.FareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StopoverFacade {
    private final LocationService locationService;
    private final StopoverService stopoverService;
    private final FareService fareService;

    private final LocationMapper locationMapper;

    public Stopover createStopover(LocationDto locationDto, LocationType stopoverType) {
        Location location = locationService.createOrGetLocation(locationDto);

        Stopover stopover = Stopover.builder()
                .location(location)
                .stopoverType(stopoverType)
                .build();

        location.addStopover(stopover);
        
        // Hibernate 는 PK 없는 객체는 비영속 상태로 간주하기에 타 엔티티와 연결 전 반드시 save
        Stopover savedStopover = stopoverService.saveStopover(stopover);

        Fare fare = fareService.createDefaultFare(savedStopover);

        savedStopover.setFare(fare);

        return stopoverService.saveStopover(savedStopover); // Fare 연결한 뒤 다시 save
    }

    public Stopover findOrCreateStopover(Party party, LocationDto locationDto) {
        Location location = locationMapper.toEntity(locationDto);

        return party.getStopovers().stream()
                .filter(stopover -> stopover.getLocation().isSameLocation(location))
                .findFirst()
                .orElseGet(() -> {
                    Stopover newStopover = createStopover(locationDto, LocationType.STOPOVER);
                    newStopover.setParty(party);
                    party.getStopovers().add(newStopover);
                    return newStopover;
                });
    }

    public boolean updateStopover(Stopover stopover, LocationDto locationDto, LocationType stopoverType) {
        Location locationEntity = (locationDto != null) ? locationService.createOrGetLocation(locationDto) : null;
        if(!stopover.update(locationEntity, stopoverType)) {
            return false;
        }

        stopoverService.saveStopover(stopover);
        return true;
    }

    public void calculateFinalFare(List<FareRequestDto> fareRequestDtoList, List<Stopover> stopoverList) {
        stopoverService.inputBaseFigure(fareRequestDtoList);

        List<Stopover> sortedStopoverList = stopoverList.stream()
                .sorted(Comparator.comparingInt(s -> s.getFare() != null ? s.getFare().getBaseFigure() : 0))
                .toList();

        int prevBaseFigure = 0;
        for(Stopover stopover : sortedStopoverList) {
            if (stopover.getStopoverType().equals(LocationType.START)) continue;

            int baseFare = stopover.getFare().getBaseFigure();
            List<PaymentStatus> psList = stopover.getPaymentStatusList();
            int numDropOff = psList.size();
            if (numDropOff == 0) continue;

            int currentFare = baseFare - prevBaseFigure;
            int expectFinalFare = currentFare / numDropOff;
            int remainderFare = currentFare % numDropOff;

            if (remainderFare > 0) {
                prevBaseFigure = (expectFinalFare + remainderFare) * numDropOff;
            } else {
                prevBaseFigure = baseFare;
            }

            for (PaymentStatus ps : psList) {
                // 최종 결산자는 항상 목적지에 내린다고 가정. 따라서 목적지에서만 Role를 검사하면 됨
                if(stopover.getStopoverType().equals(LocationType.DESTINATION)){
                    PartyMemberRole memberRole = ps.getPartyMember().getMemberRole();
                    switch (memberRole) {
                        case HOST:
                            if (!ps.getPartyMember().getAdditionalRole().equals(AdditionalRole.BOOKKEEPER)) break;
                        case BOOKKEEPER:
                            ps.setFinalFigure(expectFinalFare);
                            ps.setPaid(true);
                            break;
                        case MEMBER:
                            ps.setFinalFigure(expectFinalFare + remainderFare);
                            break;
                    }
                    continue;
                }
                ps.setFinalFigure(expectFinalFare + remainderFare);
            }
            stopoverService.saveStopover(stopover);
        }
    }
}
