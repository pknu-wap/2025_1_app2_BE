package com.wap.app2.gachitayo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wap.app2.gachitayo.Enum.RequestGenderOption;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyCreateRequestDto {
    @JsonProperty("party_start")
    private StopoverDto startLocation;
    @JsonProperty("party_destination")
    private StopoverDto destination;
    @JsonProperty("party_radius")
    private Double radius;
    @JsonProperty("party_max_person")
    private Integer maxPerson;
    @JsonProperty("party_option")
    private RequestGenderOption genderOption;
}
