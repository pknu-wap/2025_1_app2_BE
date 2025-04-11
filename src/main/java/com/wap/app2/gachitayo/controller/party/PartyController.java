package com.wap.app2.gachitayo.controller.party;

import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
import com.wap.app2.gachitayo.dto.request.PartySearchRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverAddRequestDto;
import com.wap.app2.gachitayo.dto.request.StopoverUpdateDto;
import com.wap.app2.gachitayo.dto.response.PartyCreateResponseDto;
import com.wap.app2.gachitayo.service.party.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
public class PartyController {
    private final PartyService partyService;

    @PostMapping
    public ResponseEntity<PartyCreateResponseDto> createParty(@RequestHeader("member_id") Long memberId, @RequestBody PartyCreateRequestDto requestDto) {
        return partyService.createParty(memberId, requestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getParty(@PathVariable("id") Long id) {
        return partyService.getPartyInformationById(id);
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addStopoverToParty(@PathVariable("id") Long id, @RequestHeader("host_id") Long hostId, @RequestBody StopoverAddRequestDto requestDto) {
        return partyService.addStopoverToParty(id, hostId, requestDto);
    }

    @PutMapping("/{id}/attend")
    public ResponseEntity<?> attendToParty(@RequestHeader("member_id") Long memberId, @PathVariable("id") Long partyId) {
        return partyService.attendParty(memberId, partyId);
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchPartyWithLocation(@RequestHeader("member_id") Long memberId, @RequestBody PartySearchRequestDto requestDto) {
        return partyService.searchPartyWithDestinationLocation(memberId, requestDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStopover(@PathVariable("id") Long id, @RequestBody StopoverUpdateDto requestDto) {
        return partyService.updateStopover(id, requestDto);
    }

    @GetMapping("/{id}/fare")
    public ResponseEntity<?> getPartyPaymentStatus(@PathVariable("id") Long id) {
        return partyService.getPaymentStatusAdvanced(id);
    }
}
