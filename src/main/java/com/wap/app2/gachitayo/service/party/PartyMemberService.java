package com.wap.app2.gachitayo.service.party;

import com.wap.app2.gachitayo.repository.party.PartyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartyMemberService {
    private final PartyMemberRepository partyMemberRepository;

}
