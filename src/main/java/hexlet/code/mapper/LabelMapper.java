package hexlet.code.mapper;

import hexlet.code.dto.LabelDTO;
import hexlet.code.model.Label;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LabelMapper {
    LabelDTO toDto(Label label);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Label toEntity(LabelDTO labelDTO);
}