package com.wap.app2.gachitayo.dto.response;

import com.wap.app2.gachitayo.domain.review.Review;

import java.util.List;
import java.util.Set;

public record ReviewListResponse(
        int count,
        List<MemberReviewResponse> reviews
) {
    public record MemberReviewResponse(
        String name,
        Double score,
        Set<String> tags
    ) {
        public static MemberReviewResponse from(Review review) {
            return new MemberReviewResponse(
                    review.getAuthor().getName(),
                    review.getScore(),
                    review.getTags()
            );
        }
    }
}
