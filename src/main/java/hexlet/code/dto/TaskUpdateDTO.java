package hexlet.code.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    @Size(min = 1, message = "Title must be at least 1 character")
    private String title;

    private String content;
    private Integer index;
    private Long assignee_id;
    private String status;
    private Set<Long> taskLabelIds;

}
