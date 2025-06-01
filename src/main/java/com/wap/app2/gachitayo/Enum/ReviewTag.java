package com.wap.app2.gachitayo.Enum;

import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;

import java.util.Arrays;

public enum ReviewTag {
    TIME_KEEPER("#시간 엄수"),
    GOOD_COMMUNICATION("#소통 원할"),
    KINDNESS("#친절한 태도"),
    RESPECT("#상대 존중"),
    LOCATION_CARE("#위치 배려"),
    SATISFIED("#탑승 만족"),
    GOOD_MANNER("#굿 매너"),
    RE_RIDE_HOPE("#재탑승 희망"),
    MANNER_PAYER("#매너 정산러");

    private final String value;

    ReviewTag(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ReviewTag fromValue(String value) {
        return Arrays.stream(values())
                .filter(tag -> tag.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new TagogayoException(ErrorCode.TAG_NOT_FOUND));
    }
}