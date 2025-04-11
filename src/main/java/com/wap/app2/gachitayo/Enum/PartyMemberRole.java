package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum PartyMemberRole {
    HOST, MEMBER, BOOKKEEPER;

    @JsonCreator
    public static PartyMemberRole fromString(String partyMemberRole) {
        return Arrays.stream(PartyMemberRole.values())
                .filter(r -> r.name().equalsIgnoreCase(partyMemberRole))
                .findAny()
                .orElse(null);
    }
}
