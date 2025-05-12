package com.wap.app2.gachitayo.controller.party;

import com.wap.app2.gachitayo.domain.member.MemberDetails;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverAddRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverUpdateDto;
import com.wap.app2.gachitayo.service.party.PartyFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{id}/bookkeeper")
    public ResponseEntity<?> electBookkeeper(@RequestParam("partyMemberId") Long partyMemberId, @PathVariable("id") Long partyId, @AuthenticationPrincipal MemberDetails memberDetails) {
        return partyFacade.electBookkeeper(partyId, memberDetails.getUsername(), partyMemberId);
    }
}
