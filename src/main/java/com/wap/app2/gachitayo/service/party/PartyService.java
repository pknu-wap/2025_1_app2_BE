package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.Enum.GenderOption;
import com.wap.app2.gachitayo.domain.location.Stopover;
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

    public Party createParty(List<Stopover> stopovers, Integer maxPeople, Double radius, GenderOption genderOption) {
        Party party = Party.builder()
                .stopovers(stopovers)
                .maxPeople(maxPeople)
                .allowRadius(radius)
                .genderOption(genderOption)
                .build();
        return partyRepository.save(party);
    }

    public Party findPartyById(Long id) {
        return partyRepository.findById(id).orElseThrow(() -> new TagogayoException(ErrorCode.PARTY_NOT_FOUND));
    }
}
