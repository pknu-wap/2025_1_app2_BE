package com.wap.app2.gachitayo.repository.party;

import com.wap.app2.gachitayo.Enum.JoinRequestStatus;
import com.wap.app2.gachitayo.domain.member.Member;
import com.wap.app2.gachitayo.domain.party.Party;
import com.wap.app2.gachitayo.domain.party.PartyJoinRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartyJoinRequestRepository extends JpaRepository<PartyJoinRequest, Long> {
    boolean existsByPartyAndRequesterAndStatus(Party party, Member requester, JoinRequestStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pjr FROM PartyJoinRequest pjr JOIN FETCH pjr.party WHERE pjr.id = :id")
    Optional<PartyJoinRequest> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT pjr FROM PartyJoinRequest pjr JOIN FETCH pjr.requester WHERE pjr.id = :id")
    Optional<PartyJoinRequest> findByIdWithRequester(@Param("id") Long id);
}
