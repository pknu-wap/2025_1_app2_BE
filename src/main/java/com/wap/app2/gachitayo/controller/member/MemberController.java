package com.wap.app2.gachitayo.controller.member;

import com.wap.app2.gachitayo.domain.Member.MemberDetails;
import com.wap.app2.gachitayo.dto.response.MemberProfileResponseDto;
import com.wap.app2.gachitayo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/profile")
@RequiredArgsConstructor
@RestController
@Controller
public class MemberController {

    private final MemberService memberService;

    //추후 member email or id 로 해당유저 정보 가져올 수 있게 리팩토링 예정
    @GetMapping
    public ResponseEntity<MemberProfileResponseDto> getProfile(@AuthenticationPrincipal MemberDetails memberDetails) {
        return memberService.getMemberProfile(memberDetails.getUsername());
    }
}
