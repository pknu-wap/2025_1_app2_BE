package com.wap.app2.gachitayo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MemberRole {
    HOST, MEMBER, BOOKKEEPER;

    @JsonCreator
    public static MemberRole fromString(String role) {
        for (MemberRole memberRole : MemberRole.values()) {
            if (memberRole.toString().equalsIgnoreCase(role)) {
                return memberRole;
            }
        }
        return null;
    }
}
