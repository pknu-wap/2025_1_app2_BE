package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.Enum.PartyEventType;

public record PartyUpdateNotificationDto(
        Long partyId,
        String message,
        PartyEventType eventType
) {
}
