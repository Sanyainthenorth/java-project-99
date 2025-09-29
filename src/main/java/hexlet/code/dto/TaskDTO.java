package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Integer index;
    private LocalDate createdAt;
    private Long assignee_id;
    private String title;
    private String content;
    private String status;
    private Set<LabelDTO> labels = new HashSet<>();

    public TaskDTO() {}

}
