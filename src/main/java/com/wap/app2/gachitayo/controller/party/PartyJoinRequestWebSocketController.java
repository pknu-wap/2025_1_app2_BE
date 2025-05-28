package com.wap.app2.gachitayo.controller.party;

import com.wap.app2.gachitayo.domain.member.MemberDetails;
import com.wap.app2.gachitayo.service.party.PartyFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PartyJoinRequestWebSocketController {
    private final PartyFacade partyFacade;

    @MessageMapping("/test-get-user-info")
    public void handleAction(Message<?> message, @Header("simpSessionAttributes") Map<String, Object> attributes) {
        String email = (String) attributes.get("memberEmail");
        log.info("🔐 WebSocket 요청자 이메일: {}", email);
    }


    @MessageMapping("/join-request-response")
    public void handleJoinRequest(@AuthenticationPrincipal MemberDetails memberDetails, Long partyId) {
        partyFacade.requestToJoinParty(partyId, memberDetails.getUsername());
    }
}
