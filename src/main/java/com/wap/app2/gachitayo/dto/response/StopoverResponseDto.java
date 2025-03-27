package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.dto.datadto.StopoverDto;

public record StopoverResponseDto(
        Long partyId,
        StopoverDto stopover
) {
}
