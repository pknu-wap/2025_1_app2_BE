package com.wap.app2.gachitayo.Enum;

import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;

public enum MobileCarrier {
    SKT("vmms.nate.com"),
    KT("ktfmms.magicn.com"),
    LGU("mmsmail.uplus.co.kr");

    private final String domain;

    MobileCarrier(String domain) {
        this.domain = domain;
    }

    // 도메인으로 Carrier 찾기
    public static MobileCarrier fromDomain(String domain) {
        for (MobileCarrier carrier : values()) {
            if (carrier.domain.equalsIgnoreCase(domain)) {
                return carrier;
            }
        }
        throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
    }
}
