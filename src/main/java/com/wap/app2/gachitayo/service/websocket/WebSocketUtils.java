package com.wap.app2.gachitayo.service.websocket;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import com.wap.app2.gachitayo.domain.party.PartyJoinRequest;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.response.PartyJoinRequestNotificationDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketUtils {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketUtils(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyParticipants(PartyJoinRequest request, PartyMember host, JoinRequestStatus status, String message, String hostMessage) {
        PartyJoinRequestNotificationDto toRequester =  toPartyJoinRequestNotificationDto(request, host, status, message);
        PartyJoinRequestNotificationDto toHost =  toPartyJoinRequestNotificationDto(request, host, status, hostMessage);

        messagingTemplate.convertAndSendToUser(
                request.getRequester().getEmail(),
                "/queue/join-request-response",
                toRequester
        );

        messagingTemplate.convertAndSendToUser(
                host.getMember().getEmail(),
                "/queue/join-request-response",
                toHost
        );
    }

    private PartyJoinRequestNotificationDto toPartyJoinRequestNotificationDto(PartyJoinRequest request, PartyMember host, JoinRequestStatus status, String message) {
        return PartyJoinRequestNotificationDto.builder()
                .partyId(request.getParty().getId())
                .requestId(request.getId())
                .requesterEmail(request.getRequester().getEmail())
                .hostEmail(host.getMember().getEmail())
                .status(status)
                .message(message)
                .respondedAt(request.getRespondedAt())
                .build();
    }
}
