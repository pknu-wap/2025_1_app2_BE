package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.Enum.PartyMemberRole;
import com.wap.app2.gachitayo.domain.party.PartyMember;

public record UnreviewedMemberResponse(
    Long memberId,
    String name,
    String email,
    PartyMemberRole memberRole
) {
    public static UnreviewedMemberResponse from(PartyMember partyMember) {
        return new UnreviewedMemberResponse(
            partyMember.getMember().getId(),
            partyMember.getMember().getName(),
            partyMember.getMember().getEmail(),
            partyMember.getMemberRole()
        );
    }
} 