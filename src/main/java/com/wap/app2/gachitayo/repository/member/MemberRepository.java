package com.wap.app2.gachitayo.repository.member;

import com.wap.app2.gachitayo.domain.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
