package com.wap.app2.gachitayo.service.member;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.dto.request.MemberCreateRequestDto;
import com.wap.app2.gachitayo.dto.response.MemberResponseDto;
import com.wap.app2.gachitayo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public ResponseEntity<?> signUp(MemberCreateRequestDto requestDto) {
        Member memberEntity = Member.builder()
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .gender(requestDto.getGender())
                .profileImage(requestDto.getProfileImage())
                .build();
        Long id = memberRepository.save(memberEntity).getId();
        return ResponseEntity.ok().body("Successfully created member with id " + id);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getMemberById(Long id) {
        Member memberEntity = memberRepository.findById(id).orElse(null);

        if (memberEntity == null) {
            return ResponseEntity.notFound().build();
        }

        MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                .id(memberEntity.getId())
                .name(memberEntity.getName())
                .email(memberEntity.getEmail())
                .phone(memberEntity.getPhone())
                .gender(memberEntity.getGender())
                .profileImage(memberEntity.getProfileImage())
                .build();
        return ResponseEntity.ok().body(memberResponseDto);
    }

    @Transactional(readOnly = true)
    public Member findMemberEntityById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }
}
