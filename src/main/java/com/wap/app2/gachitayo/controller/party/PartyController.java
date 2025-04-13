package com.wap.app2.gachitayo.controller.party;

import com.wap.app2.gachitayo.domain.Member.MemberDetails;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverAddRequestDto;
import com.wap.app2.gachitayo.service.party.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
public class PartyController {
    private final PartyService partyService;

    @PostMapping
    public ResponseEntity<?> createParty(@AuthenticationPrincipal MemberDetails memberDetails, @RequestBody PartyCreateRequestDto requestDto) {
        return partyService.createParty(memberDetails.getUsername(), requestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getParty(@PathVariable("id") Long id) {
        return partyService.getPartyInformationById(id);
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addStopoverToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id, @RequestBody StopoverAddRequestDto requestDto) {
        return partyService.addStopoverToParty(memberDetails.getUsername(), id, requestDto);
    }

    @PostMapping("/{id}/attend")
    public ResponseEntity<?> attendToParty(@AuthenticationPrincipal MemberDetails memberDetails, @PathVariable("id") Long id) {
        return partyService.attendParty(memberDetails.getUsername(), id);
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchPartiesWithDestinationLocation(@AuthenticationPrincipal MemberDetails memberDetails, @RequestBody PartySearchRequestDto requestDto) {
        return partyService.searchPartiesWithDestinationLocation(memberDetails.getUsername(), requestDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStopover(@PathVariable("id") Long id, @RequestBody StopoverDto stopoverDto) {
        return partyService.updateStopover(id, stopoverDto);
    }
}
