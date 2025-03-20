package com.wap.app2.gachitayo.repository;

import com.wap.app2.gachitayo.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRepository extends JpaRepository<Sample, Long> {
    public Sample findByName(String name);
}
