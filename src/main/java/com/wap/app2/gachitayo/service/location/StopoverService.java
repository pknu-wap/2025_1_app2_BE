package com.wap.app2.gachitayo.service.location;

import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.dto.request.FareRequestDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.repository.location.StopoverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class StopoverService {
    private final StopoverRepository stopoverRepository;

    public Stopover saveStopover(Stopover stopover) {
        return stopoverRepository.save(stopover);
    }

    public void inputBaseFigure(List<FareRequestDto> fareRequestDtoList) {
        fareRequestDtoList.forEach(fareRequestDto -> {
            Stopover stopover = stopoverRepository.findById(fareRequestDto.getStopoverId()).orElseThrow(() -> new TagogayoException(ErrorCode.STOPOVER_NOT_FOUND));
            stopover.getFare().setBaseFigure(fareRequestDto.getFare());
            stopoverRepository.save(stopover);
        });
    }
}
