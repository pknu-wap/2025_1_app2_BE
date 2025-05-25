package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.Enum.MobileCarrier;

public record SmsInfoResponse(
        String phoneNumber,
        MobileCarrier carrier
) {
}
