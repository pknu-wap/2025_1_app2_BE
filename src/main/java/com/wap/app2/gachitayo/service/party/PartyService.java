package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.repository.party.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyService {
    private final PartyRepository partyRepository;

    public Party findPartyById(Long id) {
        return partyRepository.findById(id).orElseThrow(() -> new TagogayoException(ErrorCode.PARTY_NOT_FOUND));
    }

    public Party saveParty(Party party) {
        return partyRepository.save(party);
    }

    public Party findPartyWithStopovers(Long id) {
        return partyRepository.findPartyWithStopovers(id).orElseThrow(() -> new TagogayoException(ErrorCode.PARTY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Party> findPartiesWithinRadius(double latitude, double longitude, double radius) {
        return partyRepository.findPartiesWithRadius(latitude, longitude, radius);
    }
}
