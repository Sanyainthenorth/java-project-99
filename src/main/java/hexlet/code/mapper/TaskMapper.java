package hexlet.code.mapper;

import hexlet.code.model.Task;
import hexlet.code.model.Label;
import hexlet.code.dto.TaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus.slug", target = "status")
    @Mapping(source = "assignee.id", target = "assignee_id")
    @Mapping(target = "taskLabelIds", expression = "java(mapLabelIds(task.getLabels()))")
    TaskDTO toDto(Task task);

    default Set<Long> mapLabelIds(Set<Label> labels) {
        if (labels == null) {
            return new HashSet<>();
        }

        Set<Long> result = new HashSet<>();
        for (Label label : labels) {
            result.add(label.getId());
        }
        return result;
    }
}