package com.wap.app2.gachitayo.dto.datadto;

import com.wap.app2.gachitayo.Enum.MemberRole;
import com.wap.app2.gachitayo.dto.response.MemberResponseDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyMemberDto {
    MemberResponseDto member;
    MemberRole memberRole;
}
