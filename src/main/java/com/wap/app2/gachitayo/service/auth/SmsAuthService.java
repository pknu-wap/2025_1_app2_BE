package com.wap.app2.gachitayo.service.auth;

import com.wap.app2.gachitayo.Enum.MobileCarrier;
import com.wap.app2.gachitayo.domain.auth.ImapStoreManager;
import com.wap.app2.gachitayo.dto.request.SmsKeyVerifyRequest;
import com.wap.app2.gachitayo.dto.response.SmsInfoResponse;
import com.wap.app2.gachitayo.dto.response.SmsKeyResponse;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import jakarta.mail.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.regex.Matcher;

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
            SmsInfoResponse smsInfo = findSmsInfoFromLatestMessages(request.key());

            // 검증
            if (smsInfo == null)
                throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);

            // 4. 검증된 전화번호 저장
            storeVerifiedPhoneNumber(request.key(), smsInfo.phoneNumber());

            return ResponseEntity.ok(smsInfo);

        } catch (Exception e) {
            throw new TagogayoException(ErrorCode.NOT_VERIFIED_SMS);
        }
    }

    private SmsInfoResponse findSmsInfoFromAttachment(Message message, String searchKey, String phoneNumber, String domain) {
        try {
            Object content = message.getContent();
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                for (int k = 0; k < multipart.getCount(); k++) {
                    BodyPart bodyPart = multipart.getBodyPart(k);
                    String fileName = bodyPart.getFileName();
                    String contentType = bodyPart.getContentType();
                    if (fileName != null
                            && contentType.toLowerCase().contains("text/plain")
                            && contentType.toLowerCase().contains("euc-kr")) {
                        try (InputStream is = bodyPart.getInputStream();
                             java.util.Scanner scanner = new java.util.Scanner(is, "EUC-KR")) {
                            scanner.useDelimiter("\\A");
                            String textFileContent = scanner.hasNext() ? scanner.next() : "";
                            if (textFileContent != null && textFileContent.contains(searchKey)) {
                                return new SmsInfoResponse(phoneNumber, MobileCarrier.fromDomain(domain));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 첨부파일 처리 중 예외 발생 시 무시
        }
        return null;
    }

    private SmsInfoResponse findSmsInfoFromBody(Message message, String searchKey, String phoneNumber, String domain) {
        try {
            Object content = message.getContent();
            String body = null;
            if (content instanceof String) {
                body = (String) content;
            } else if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                for (int k = 0; k < multipart.getCount(); k++) {
                    BodyPart bodyPart = multipart.getBodyPart(k);
                    if (bodyPart.getContentType().toLowerCase().contains("text/plain")) {
                        Object partContent = bodyPart.getContent();
                        if (partContent instanceof String) {
                            body = (String) partContent;
                            break;
                        }
                    }
                }
            }
            if (body != null && body.contains(searchKey)) {
                return new SmsInfoResponse(phoneNumber, MobileCarrier.fromDomain(domain));
            }
        } catch (Exception e) {
            // 본문 처리 중 예외 발생 시 무시
        }
        return null;
    }

    public SmsInfoResponse findSmsInfoFromLatestMessages(String searchKey) {
        Folder inbox = null;
        try {
            Store store = manager.getStore();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int totalMessages = inbox.getMessageCount();
            int start = Math.max(1, totalMessages - 2); // 최근 3개 메일의 시작 인덱스
            int end = totalMessages; // 마지막(가장 최근) 메일 인덱스

            Message[] list = inbox.getMessages(start, end);

            for (int i = list.length - 1; i >= 0; i--) {
                Message message = list[i];
                String fromEmail = message.getFrom()[0].toString();
                // "이름 <이메일>" 형식에서 이메일만 추출
                String email = fromEmail;
                Matcher matcher = java.util.regex.Pattern.compile("<([^<>@\\s]+@[^<>@\\s]+)>").matcher(fromEmail);
                if (matcher.find()) {
                    email = matcher.group(1);
                }

                // 도메인 추출
                String[] emailParts = email.split("@");
                if (emailParts.length != 2) continue;
                String domain = emailParts[1];
                String phoneNumber = emailParts[0];

                // ktfmms.magicn.com 도메인일 경우 첨부파일에서 searchKey 포함 여부 확인
                if ("ktfmms.magicn.com".equalsIgnoreCase(domain)) {
                    SmsInfoResponse smsInfo = findSmsInfoFromAttachment(message, searchKey, phoneNumber, domain);
                    if (smsInfo != null) return smsInfo;
                } else {
                    // 그 외 도메인은 본문에 searchKey가 있는지 확인
                    SmsInfoResponse smsInfo = findSmsInfoFromBody(message, searchKey, phoneNumber, domain);
                    if (smsInfo != null) return smsInfo;
                }
            }

            return null;
        } catch (Exception e) {
            return null;
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