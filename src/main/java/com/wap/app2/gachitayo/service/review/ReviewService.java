package com.wap.app2.gachitayo.service.review;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.review.Review;
import com.wap.app2.gachitayo.dto.request.ReviewMemberRequest;
import com.wap.app2.gachitayo.error.exception.ErrorCode;
import com.wap.app2.gachitayo.error.exception.TagogayoException;
import com.wap.app2.gachitayo.repository.member.MemberRepository;
import com.wap.app2.gachitayo.repository.member.ReviewRepository;
import com.wap.app2.gachitayo.service.party.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final PartyService partyService;

    public ResponseEntity<?> addMemberReview(String authorEmail, String targetEmail, ReviewMemberRequest request) {
        Member author = findMemberByEmail(authorEmail);
        Member target = findMemberByEmail(targetEmail);
        Party party = partyService.findPartyById(request.party_id());

        if (!isMemberInParty(party, target)) {
            throw new TagogayoException(ErrorCode.NOT_IN_PARTY);
        }

        if (isExistReview(party, author, target)) {
            throw new TagogayoException(ErrorCode.ALREADY_REVIEW);
        }

        Review review = createReview(author, target, party, request);
        reviewRepository.save(review);

        return ResponseEntity.ok(Map.of("message", "리뷰 작성이 되었습니다."));
    }

    public double getReviewRateByMember(Long memberId) {
        List<Review> reviews = reviewRepository.findByTargetId(memberId);

        double total_score = reviews.stream()
                .mapToDouble(Review::getScore)
                .average()
                .orElse(0.0);

        //소수 둘째자리까지 반올림
        return Math.round(total_score * 100) / 100.0;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new TagogayoException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private boolean isMemberInParty(Party party, Member target) {
        return party.getPartyMemberList().stream()
                .anyMatch(partyMember -> partyMember.getMember().getId().equals(target.getId()));
    }

    private boolean isExistReview(Party party, Member author, Member target) {
        return reviewRepository.existsByPartyIdAndAuthorIdAndTargetId(party.getId(), author.getId(), target.getId());
    }

    private Review createReview(Member author, Member target, Party party, ReviewMemberRequest request) {
        return Review.builder()
                .author(author)
                .target(target)
                .party(party)
                .score(request.score())
                .contents(request.contents())
                .build();
    }
}