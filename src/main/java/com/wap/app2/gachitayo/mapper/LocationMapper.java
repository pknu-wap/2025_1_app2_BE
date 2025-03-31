package com.wap.app2.gachitayo.mapper;

import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    Location toEntity(LocationDto dto);

    LocationDto toDto(Location entity);
}
