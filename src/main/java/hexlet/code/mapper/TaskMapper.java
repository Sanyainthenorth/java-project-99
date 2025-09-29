package hexlet.code.mapper;

import hexlet.code.dto.LabelDTO;
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
    @Mapping(target = "labels", expression = "java(mapLabels(task.getLabels()))") // ДОБАВЬ ЭТУ СТРОКУ!
    TaskDTO toDto(Task task);

    // Простой маппинг для лейблов
    default Set<LabelDTO> mapLabels(Set<Label> labels) {
        if (labels == null) return new HashSet<>();

        Set<LabelDTO> result = new HashSet<>();
        for (Label label : labels) {
            LabelDTO dto = new LabelDTO();
            dto.setId(label.getId());
            dto.setName(label.getName());
            dto.setCreatedAt(label.getCreatedAt());
            result.add(dto);
        }
        return result;
    }
}