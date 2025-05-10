package com.wap.app2.gachitayo.controller.review;

import com.wap.app2.gachitayo.domain.member.MemberDetails;
import com.wap.app2.gachitayo.dto.request.ReviewMemberRequest;
import com.wap.app2.gachitayo.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/{email}/review")
@RequiredArgsConstructor
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> addMemberReview(@PathVariable String email, @AuthenticationPrincipal MemberDetails memberDetails, @RequestBody @Validated ReviewMemberRequest request) {
        return reviewService.addMemberReview(memberDetails.getUsername(), email, request);
    }
}
