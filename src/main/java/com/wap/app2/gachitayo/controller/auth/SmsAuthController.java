package com.wap.app2.gachitayo.controller.auth;

import com.wap.app2.gachitayo.dto.request.SmsKeyVerifyRequest;
import com.wap.app2.gachitayo.service.auth.SmsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@RestController
@Controller
public class SmsAuthController {
    private final SmsAuthService smsAuthService;

    @GetMapping("/sms")
    public ResponseEntity<?> getSmsSessionKey() {
        return smsAuthService.getSmsSessionKey();
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<?> verifySmsSessionKey(@RequestBody @Validated SmsKeyVerifyRequest request) {
        return smsAuthService.verifySmsSessionKey(request);
    }
}
