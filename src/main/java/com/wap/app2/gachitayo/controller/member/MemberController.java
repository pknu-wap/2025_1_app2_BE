package com.wap.app2.gachitayo.controller.member;

import com.wap.app2.gachitayo.dto.request.MemberCreateRequestDto;

import com.wap.app2.gachitayo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody MemberCreateRequestDto requestDto) {
        return memberService.signUp(requestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMember(@PathVariable("id") Long id) {
        return memberService.getMemberById(id);
    }
}
