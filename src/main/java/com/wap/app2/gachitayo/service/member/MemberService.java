package com.wap.app2.gachitayo.service.member;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.dto.request.EditMemberRequest;
import com.wap.app2.gachitayo.dto.response.MemberProfileResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.repository.member.MemberRepository;
import com.wap.app2.gachitayo.service.review.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReviewService reviewService;

    public ResponseEntity<MemberProfileResponseDto> getMemberProfile(String email) {
        Member member = this.getUserByEmail(email);

        if (member == null) {
            throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
        }

        double review_rate = reviewService.getReviewRateByMember(member.getId());

        MemberProfileResponseDto dto = new MemberProfileResponseDto(
                member.getName(),
                member.getPhone(),
                member.getAge(),
                member.getGender(),
                member.getProfileImageUrl(),
                member.getEmail(),
                review_rate
        );

        return ResponseEntity.ok(dto);
    }

    @Transactional
    public ResponseEntity<MemberProfileResponseDto> changeName(String email, EditMemberRequest editMemberRequest) {
        Member member = this.getUserByEmail(email);

        if (member == null) {
            throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
        }

        double review_rate = reviewService.getReviewRateByMember(member.getId());

        member.setName(editMemberRequest.name());

        MemberProfileResponseDto dto = new MemberProfileResponseDto(
                member.getName(),
                member.getPhone(),
                member.getAge(),
                member.getGender(),
                member.getProfileImageUrl(),
                member.getEmail(),
                review_rate
        );

        return ResponseEntity.ok(dto);
    }

    public Member getUserByEmail(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }
}
