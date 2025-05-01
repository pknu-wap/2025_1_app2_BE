package com.wap.app2.gachitayo.mapper;

import com.wap.app2.gachitayo.domain.location.Location;
import com.wap.app2.gachitayo.dto.datadto.LocationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationDto dto);

    LocationDto toDto(Location entity);

    default Location toEntityWithExisting(LocationDto dto, Location existingEntity) {
        existingEntity.setAddress(dto.getAddress());
        existingEntity.setLatitude(dto.getLatitude());
        existingEntity.setLongitude(dto.getLongitude());
        return existingEntity;
    }
}
