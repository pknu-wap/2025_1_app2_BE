package com.wap.app2.gachitayo.service.websocket;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import com.wap.app2.gachitayo.Enum.PartyEventType;
import com.wap.app2.gachitayo.domain.party.PartyJoinRequest;
import com.wap.app2.gachitayo.domain.party.PartyMember;
import com.wap.app2.gachitayo.dto.response.PartyJoinRequestNotificationDto;
import com.wap.app2.gachitayo.dto.response.PartyUpdateNotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PartyJoinRequestWebSocketUtils {
    public static final String BROADCAST_FOR_EXTERNAL = "/topic/parties/public-updates";
    public static final String BROADCAST_FOR_INTERNAL = "/topic/party/";

    private final SimpMessagingTemplate messagingTemplate;

    public PartyJoinRequestWebSocketUtils(SimpMessagingTemplate messagingTemplate) {
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

    public void broadcastPartyUpdate(Long partyId, String destination, String message, PartyEventType eventType) {
        PartyUpdateNotificationDto dto = new PartyUpdateNotificationDto(partyId, message, eventType);
        if (destination.equals(BROADCAST_FOR_INTERNAL)) {
            destination = BROADCAST_FOR_INTERNAL + partyId + "/members";
        }
        messagingTemplate.convertAndSend(destination, dto);
        log.info("[브로드캐스트 알림 전송] 변경된 파티 id: {}, destination: {}, payload: {}", partyId, destination, dto);
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
