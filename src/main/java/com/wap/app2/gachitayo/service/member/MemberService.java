package com.wap.app2.gachitayo.service.member;

import com.wap.app2.gachitayo.domain.Member.Member;
import com.wap.app2.gachitayo.domain.Member.Review;
import com.wap.app2.gachitayo.dto.request.EditMemberRequest;
import com.wap.app2.gachitayo.dto.response.MemberProfileResponseDto;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.repository.member.MemberRepository;
import com.wap.app2.gachitayo.repository.member.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    public ResponseEntity<MemberProfileResponseDto> getMemberProfile(String email) {
        Member member = this.getUserByEmail(email);

        if (member == null) {
            throw new TagogayoException(ErrorCode.MEMBER_NOT_FOUND);
        }

        double review_rate = getReviewRateByMember(member);

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

        double review_rate = getReviewRateByMember(member);

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

    private double getReviewRateByMember(Member member) {
        List<Review> reviews = reviewRepository.findAllByMember(member).orElseGet(ArrayList::new);

        double total_score = reviews.stream()
                            .mapToDouble(Review::getScore)
                            .average()
                            .orElse(0.0);
        //소수 둘째자리까지 반올림
        return Math.round(total_score * 100) / 100.0;
    }

    public Member getUserByEmail(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }
}
