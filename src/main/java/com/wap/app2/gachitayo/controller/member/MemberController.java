package com.wap.app2.gachitayo.controller.member;

import com.wap.app2.gachitayo.domain.member.MemberDetails;
import com.wap.app2.gachitayo.dto.request.EditMemberRequest;
import com.wap.app2.gachitayo.dto.request.MemberReportRequest;
import com.wap.app2.gachitayo.dto.response.MemberProfileResponseDto;
import com.wap.app2.gachitayo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
@Controller
public class MemberController {

    private final MemberService memberService;

    //추후 member email or id 로 해당유저 정보 가져올 수 있게 리팩토링 예정
    @GetMapping("/profile")
    public ResponseEntity<MemberProfileResponseDto> getProfile(@AuthenticationPrincipal MemberDetails memberDetails) {
        return memberService.getMemberProfile(memberDetails.getUsername());
    }

    @PatchMapping("/profile")
    public ResponseEntity<MemberProfileResponseDto> changeName(@AuthenticationPrincipal MemberDetails memberDetails, @RequestBody EditMemberRequest request) {
        return memberService.changeName(memberDetails.getUsername(), request);
    }

    @PostMapping("/report")
    public ResponseEntity<?> reportMember(@AuthenticationPrincipal MemberDetails memberDetails,
                                          @Validated @RequestBody MemberReportRequest request) {
        return memberService.reportMember(memberDetails.getUsername(), request);
    }
}
