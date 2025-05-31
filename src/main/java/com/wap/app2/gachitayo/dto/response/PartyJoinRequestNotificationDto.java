package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PartyJoinRequestNotificationDto(
        Long partyId,
        Long requestId,
        String requesterEmail,
        String hostEmail,
        JoinRequestStatus status,
        String message,
        LocalDateTime respondedAt
) {
}
