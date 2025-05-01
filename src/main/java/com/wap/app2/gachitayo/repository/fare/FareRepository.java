package com.wap.app2.gachitayo.repository.fare;

import com.wap.app2.gachitayo.domain.fare.Fare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FareRepository extends JpaRepository<Fare, Long> {
}
