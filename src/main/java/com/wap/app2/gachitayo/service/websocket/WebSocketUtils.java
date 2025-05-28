package com.wap.app2.gachitayo.service.websocket;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import com.wap.app2.gachitayo.domain.party.PartyJoinRequest;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.response.PartyJoinRequestNotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketUtils {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketUtils(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyParticipants(PartyJoinRequest request, PartyMember host, JoinRequestStatus status, String message, String hostMessage) {
        PartyJoinRequestNotificationDto toRequester =  toPartyJoinRequestNotificationDto(request, host, status, message);
        PartyJoinRequestNotificationDto toHost =  toPartyJoinRequestNotificationDto(request, host, status, hostMessage);

        log.info("[알림 전송] 대상: {}, destination: {}, payload: {}",
                request.getRequester().getEmail(), "/queue/join-request-response", toRequester);
        messagingTemplate.convertAndSendToUser(
                request.getRequester().getEmail(),
                "/queue/join-request-response",
                toRequester
        );

        log.info("[알림 전송] 대상: {}, destination: {}, payload: {}",
                host.getMember().getEmail(), "/queue/join-request-response", toHost);
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
