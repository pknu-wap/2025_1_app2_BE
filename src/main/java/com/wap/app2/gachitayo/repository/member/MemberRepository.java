package com.wap.app2.gachitayo.repository.member;

import com.wap.app2.gachitayo.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
