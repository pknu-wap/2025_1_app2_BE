package com.wap.app2.gachitayo.controller;

import com.wap.app2.gachitayo.entity.dto.SampleRequestDto;
import com.wap.app2.gachitayo.entity.dto.SampleResponseDto;
import com.wap.app2.gachitayo.service.SampleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sample")
public class SampleController {
    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping
    public ResponseEntity<?> getSample(@RequestBody SampleRequestDto sampleRequestDto) {
        SampleResponseDto responseDto = null;
        try{
            responseDto = sampleService.findSample(sampleRequestDto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to find sample");
        }
        return ResponseEntity.ok().body(responseDto);
    }

    @PostMapping("")
    public ResponseEntity<String> postSample(@RequestBody SampleRequestDto dto) {
        try{
            sampleService.save(dto);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to save sample");
        }
        return ResponseEntity.ok().body("Success save sample");
    }
}
