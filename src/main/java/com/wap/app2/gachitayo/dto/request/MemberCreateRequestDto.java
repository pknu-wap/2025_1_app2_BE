package com.wap.app2.gachitayo.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wap.app2.gachitayo.Enum.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberCreateRequestDto {
    @NotNull
    private String name;
    @Pattern(regexp = "^010-?([0-9]{3,4})-?([0-9]{4})$")
    private String phone;
    @NotNull
    @Email
    private String email;
    private Gender gender;
    private String profileImage;
}
