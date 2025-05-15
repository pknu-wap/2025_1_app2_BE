package com.wap.app2.gachitayo.controller.party;

import com.wap.app2.gachitayo.domain.member.MemberDetails;
import com.wap.app2.gachitayo.dto.request.*;
import com.wap.app2.gachitayo.service.party.PartyFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
public class PartyController {
    private final PartyFacade partyFacade;

    @PostMapping
    public ResponseEntity<?> createParty(@AuthenticationPrincipal MemberDetails memberDetails, @RequestBody PartyCreateRequestDto requestDto) {
        return partyFacade.createParty(memberDetails.getUsername(), requestDto);
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addStopoverToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody StopoverAddRequestDto requestDto) {
        return partyFacade.addStopoverToParty(memberDetails.getUsername(), id, requestDto);
    }

    @PostMapping("/{id}/attend")
    public ResponseEntity<?> attendToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id) {
        return partyFacade.attendParty(memberDetails.getUsername(), id);
    }

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
}
