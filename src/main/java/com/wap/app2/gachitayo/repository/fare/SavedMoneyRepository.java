package com.wap.app2.gachitayo.repository.fare;

import com.wap.app2.gachitayo.domain.fare.SavedMoney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavedMoneyRepository extends JpaRepository<SavedMoney, Long> {
    @Query("SELECT COALESCE(SUM(sm.savedAmount), 0) FROM SavedMoney sm WHERE sm.partyMember.member.id = :memberId")
    int sumSavedAmountByMemberId(@Param("memberId") Long memberId);
} 