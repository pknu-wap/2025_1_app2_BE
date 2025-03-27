package com.wap.app2.gachitayo.mapper;

import com.wap.app2.gachitayo.domain.location.Stopover;
import com.wap.app2.gachitayo.dto.datadto.StopoverDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = LocationMapper.class)
public interface StopoverMapper {
    StopoverMapper INSTANCE = Mappers.getMapper(StopoverMapper.class);

    @Mapping(source = "location", target = "location")
    @Mapping(source = "stopoverType", target = "stopoverType")
    StopoverDto toDto(Stopover entity);
}
