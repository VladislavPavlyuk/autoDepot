package com.example.autodepot.mapper;

import com.example.autodepot.dto.ErrorAuditCreateDTO;
import com.example.autodepot.dto.ErrorAuditDTO;
import com.example.autodepot.entity.ErrorAudit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ErrorAuditMapper {

    @Mapping(target = "id", ignore = true)
    ErrorAudit toEntity(ErrorAuditCreateDTO dto);

    ErrorAuditDTO toDto(ErrorAudit entity);
}
