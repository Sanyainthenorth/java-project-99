package hexlet.code.mapper;

import hexlet.code.model.Task;
import hexlet.code.dto.TaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus.name", target = "status")
    @Mapping(source = "assignee.id", target = "assignee_id")
    TaskDTO toDto(Task task);
}
