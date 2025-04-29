package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import lombok.Builder;

import java.util.List;

@Builder
public record StopoverAndPartyMemberResponseDto(
        StopoverDto stopover,
        List<PartyMemberResponseDto> partyMembers
) {
}
