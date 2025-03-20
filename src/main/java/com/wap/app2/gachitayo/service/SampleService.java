package com.wap.app2.gachitayo.service;

import com.wap.app2.gachitayo.entity.Sample;
import com.wap.app2.gachitayo.entity.dto.SampleRequestDto;
import com.wap.app2.gachitayo.entity.dto.SampleResponseDto;
import com.wap.app2.gachitayo.repository.SampleRepository;
import org.springframework.stereotype.Service;

@Service
public class SampleService {
    private final SampleRepository sampleRepository;

    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public void save(SampleRequestDto dto) {
        String sampleName = dto.getName();
        System.out.println(sampleName);
        Sample sample = new Sample(sampleName);
        Long id = sampleRepository.save(sample).getId();
        System.out.println(id);
    }

    public SampleResponseDto findSample(SampleRequestDto dto) {
        Sample sample = sampleRepository.findByName(dto.getName());
        if (sample == null) return null;
        return new SampleResponseDto(sample.getId(), sample.getName());
    }
}
