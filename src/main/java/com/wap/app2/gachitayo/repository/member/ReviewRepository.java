package com.wap.app2.gachitayo.repository.member;

import com.wap.app2.gachitayo.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetId(Long targetId);
    boolean existsByPartyIdAndAuthorIdAndTargetId(Long partyId, Long authorId, Long targetId);
    List<Review> findByPartyIdAndAuthorId(Long partyId, Long authorId);
}
