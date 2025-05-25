package com.wap.app2.gachitayo.service.auth;

import com.wap.app2.gachitayo.Enum.MobileCarrier;
import com.wap.app2.gachitayo.domain.auth.ImapStoreManager;
import com.wap.app2.gachitayo.dto.request.SmsKeyVerifyRequest;
import com.wap.app2.gachitayo.dto.response.SmsInfoResponse;
import com.wap.app2.gachitayo.dto.response.SmsKeyResponse;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import jakarta.mail.*;
import jakarta.mail.search.BodyTerm;
import jakarta.mail.search.SearchTerm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SmsAuthService {
    @Value("${spring.sms.email}")
    private String verify_email;
    private final Duration sessionTimeout = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final ImapStoreManager manager;

    public ResponseEntity<?> getSmsSessionKey() {
        String sessionKey = getSessionKey(15);

        redisTemplate.opsForValue().set(sessionKey, "PENDING", Duration.ofMinutes(5));

        return ResponseEntity.ok(new SmsKeyResponse(
                verify_email,
                sessionKey
        ));
    }

    public ResponseEntity<?> verifySmsSessionKey(SmsKeyVerifyRequest request) {
        try {
            // 1. 세션 검증
            validatePendingSession(request.key());

            // 2. 메시지 검색
            Address[] fromAddresses = findLatestMessageContaining(request.key());

            if (fromAddresses == null || fromAddresses.length != 1) throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);

            String fromEmail = fromAddresses[0].toString();
            if (isValidEmail(fromEmail) == false) throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
            SmsInfoResponse smsInfo = parseSmsInfo(fromEmail);

            // 4. 검증된 전화번호 저장
            storeVerifiedPhoneNumber(request.key(), smsInfo.phoneNumber());

            return ResponseEntity.ok(smsInfo);

        } catch (Exception e) {
            throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
        }
    }

    public SmsInfoResponse parseSmsInfo(String fromEmail) {
        String[] parts = fromEmail.split("@");

        String phoneNumber = parts[0];
        String domain = parts[1];

        if (isValidPhoneNumber(phoneNumber)) throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
        MobileCarrier carrier = MobileCarrier.fromDomain(domain);

        return new SmsInfoResponse(phoneNumber, carrier);
    }

    public Address[] findLatestMessageContaining(String searchKey) {
        Folder inbox = null;
        try {
            Store store = manager.getStore();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            SearchTerm bodyTerm = new BodyTerm(searchKey);
            Message[] foundMessages = inbox.search(bodyTerm);

            if (foundMessages.length == 0) {
                throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
            }

            return foundMessages[foundMessages.length - 1].getFrom();

        } catch (Exception e) {
            throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
        } finally {
            closeInboxSafely(inbox);
        }
    }

    private void closeInboxSafely(Folder inbox) {
        if (inbox != null && inbox.isOpen()) {
            try {
                inbox.close(false);
            } catch (MessagingException ignored) {
                //무시
            }
        }
    }

    public void validatePendingSession(String key) {
        String sessionValue = redisTemplate.opsForValue().get(key);

        if (sessionValue == null) {
            throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
        }

        //이미 검증 후 재 검증 삭제
        if (!sessionValue.equals("PENDING")) {
            redisTemplate.delete(key);
            throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
        }
    }

    public void storeVerifiedPhoneNumber(String key, String phoneNumber) {
        redisTemplate.opsForValue().set(key, phoneNumber, sessionTimeout);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String numberRegex = "^01([0|1|6|7|8|9])-?([0-9]{3,4})-?([0-9]{4})$";
        return phoneNumber.matches(numberRegex);
    }

    private boolean isValidEmail(String email) {
        String[] list = email.split("@");
        return list.length == 2;
    }

    public String getSessionKey(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*_";
        StringBuilder sb = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }
}