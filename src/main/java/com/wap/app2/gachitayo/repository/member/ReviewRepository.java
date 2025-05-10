package com.wap.app2.gachitayo.repository.member;

import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.member.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<List<Review>> findAllByMember(Member member);
}
