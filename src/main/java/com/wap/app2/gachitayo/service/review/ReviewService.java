package com.wap.app2.gachitayo.service.review;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.member.Review;
import com.wap.app2.gachitayo.repository.member.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public double getReviewRateByMember(Long memberId) {
        List<Review> reviews = reviewRepository.findByMemberId(memberId);

        double total_score = reviews.stream()
                .mapToDouble(Review::getScore)
                .average()
                .orElse(0.0);

        //소수 둘째자리까지 반올림
        return Math.round(total_score * 100) / 100.0;
    }
}
