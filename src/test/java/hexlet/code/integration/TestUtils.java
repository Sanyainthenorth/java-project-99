package hexlet.code.integration;

import hexlet.code.dto.TaskStatusUpdateDTO;
import org.openapitools.jackson.nullable.JsonNullable;

public class TestUtils {

    public static <T> JsonNullable<T> of(T value) {
        return JsonNullable.of(value);
    }

    public static TaskStatusUpdateDTO createTaskStatusUpdateDTO(String name, String slug) {
        TaskStatusUpdateDTO dto = new TaskStatusUpdateDTO();
        if (name != null) {
            dto.setName(JsonNullable.of(name));
        }
        if (slug != null) {
            dto.setSlug(JsonNullable.of(slug));
        }
        return dto;
    }
}