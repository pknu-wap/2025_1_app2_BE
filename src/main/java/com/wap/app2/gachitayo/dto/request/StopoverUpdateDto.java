package com.wap.app2.gachitayo.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.LocationType;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopoverUpdateDto {
    @NotNull
    @JsonProperty("stopover_id")
    private Long stopoverId;
    @Email
    @JsonProperty("member_email")
    private String memberEmail;
    private LocationDto location;
    @JsonProperty("stopover_type")
    private LocationType stopoverType;
}
