package com.wap.app2.gachitayo.controller.party;

import com.wap.app2.gachitayo.domain.member.MemberDetails;
import com.wap.app2.gachitayo.dto.request.*;
import com.wap.app2.gachitayo.service.party.PartyFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
public class PartyController {
    private final PartyFacade partyFacade;

    @GetMapping
    public ResponseEntity<?> getMyParties(@AuthenticationPrincipal MemberDetails memberDetails) {
        return partyFacade.getMyPartyList(memberDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<?> createParty(@AuthenticationPrincipal MemberDetails memberDetails, @RequestBody PartyCreateRequestDto requestDto) {
        return partyFacade.createParty(memberDetails.getUsername(), requestDto);
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addStopoverToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody StopoverAddRequestDto requestDto) {
        return partyFacade.addStopoverToParty(memberDetails.getUsername(), id, requestDto);
    }

//    @PostMapping("/{id}/attend")
//    public ResponseEntity<?> attendToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id) {
//        return partyFacade.attendParty(memberDetails.getUsername(), id);
//    }

    @PostMapping("/search")
    public ResponseEntity<?> searchPartiesWithDestinationLocation(@AuthenticationPrincipal MemberDetails memberDetails, @RequestBody PartySearchRequestDto requestDto) {
        return partyFacade.searchPartiesWithDestinationLocation(memberDetails.getUsername(), requestDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStopover(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody StopoverUpdateDto updateDto) {
        return partyFacade.updateStopover(memberDetails.getUsername(), id, updateDto);
    }

    @PatchMapping("/{partyId}/member/{partyMemberId}/bookkeeper")
    public ResponseEntity<?> electBookkeeper(@PathVariable("partyId") Long partyId, @PathVariable("partyMemberId") Long partyMemberId, @AuthenticationPrincipal MemberDetails memberDetails) {
        return partyFacade.electBookkeeper(partyId, memberDetails.getUsername(), partyMemberId);
    }

    @PostMapping("/{id}/fare")
    public ResponseEntity<?> reflectCalculatedFare(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody List<FareRequestDto> requestDtoList) {
        return partyFacade.reflectCalculatedFare(id, memberDetails.getUsername(), requestDtoList);
    }

    @PatchMapping("/{id}/fare/confirm")
    public ResponseEntity<?> reflectConfirmedFare(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody FareConfirmRequestDto requestDto) {
        return partyFacade.reflectPayment(id, memberDetails.getUsername(), requestDto);
    }

    @EventListener
    public void handleSessionConnectedEvent(SessionConnectEvent event) {
        Principal user = event.getUser();
        System.out.println("✅ WebSocket 연결됨 - 사용자: " + (user != null ? user.getName() : "익명"));
    }

    // attendance
    @PostMapping("/{id}/attend")
    public ResponseEntity<?> attendToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id) {
        partyFacade.requestToJoinParty(id, memberDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/attend/accept")
    public ResponseEntity<?> acceptToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody PartyJoinRequestSocketRequestDto requestDto) {
        partyFacade.acceptJoinRequest(id, requestDto, memberDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/attend/reject")
    public ResponseEntity<?> rejectToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody PartyJoinRequestSocketRequestDto requestDto) {
        partyFacade.rejectJoinRequest(id, requestDto, memberDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/attend/cancel")
    public ResponseEntity<?> cancelToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody PartyJoinRequestSocketRequestDto requestDto) {
        partyFacade.cancelJoinRequest(id, requestDto, memberDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
