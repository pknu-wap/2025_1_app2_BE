package com.wap.app2.gachitayo.controller.party;

import com.wap.app2.gachitayo.domain.Member.MemberDetails;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
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
        if(memberDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Member details cannot be null");
        }
        return partyService.createParty(memberDetails.getUsername(), requestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getParty(@PathVariable("id") Long id) {
        return partyService.getPartyInformationById(id);
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addStopoverToParty(@PathVariable("id") Long id, @RequestBody StopoverDto stopoverDto) {
        return partyService.addStopoverToParty(id, stopoverDto);
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchPartyWithLocation(@RequestBody PartySearchRequestDto requestDto) {
        return partyService.searchPartyWithDestinationLocation(requestDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStopover(@PathVariable("id") Long id, @RequestBody StopoverDto stopoverDto) {
        return partyService.updateStopover(id, stopoverDto);
    }
}
