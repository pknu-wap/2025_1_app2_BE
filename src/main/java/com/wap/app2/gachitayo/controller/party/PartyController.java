package com.wap.app2.gachitayo.controller.party;

import com.wap.app2.gachitayo.dto.request.PartyCreateRequestDto;
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
    public ResponseEntity<PartyCreateResponseDto> createParty(@RequestBody PartyCreateRequestDto requestDto) {
        return partyService.createParty(requestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getParty(@PathVariable Long id) {
        return partyService.getPartyInformationById(id);
    }
}
