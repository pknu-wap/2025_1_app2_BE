package com.wap.app2.gachitayo.service.member;

import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.Member.MemberDetails;
import com.wap.app2.gachitayo.dto.response.MemberProfileResponseDto;
import com.wap.app2.gachitayo.repository.auth.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public ResponseEntity<MemberProfileResponseDto> getMemberProfile(MemberDetails memberDetails) {
        Member member = memberDetails.getUser();

        MemberProfileResponseDto dto = new MemberProfileResponseDto(
                member.getName(),
                member.getPhone(),
                member.getAge(),
                member.getGender(),
                member.getProfileImageUrl(),
                member.getEmail()
        );

        return ResponseEntity.ok(dto);
    }

    public Member getUserByEmail(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }
}
