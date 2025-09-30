package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskFilterParams {
    private String titleCont;
    private Long assigneeId;
    private String status;
    private Long labelId;

    public TaskFilterParams() {
    }

    public TaskFilterParams(String titleCont, Long assigneeId, String status, Long labelId) {
        this.titleCont = titleCont;
        this.assigneeId = assigneeId;
        this.status = status;
        this.labelId = labelId;
    }

    public boolean hasTitleFilter() {
        return titleCont != null && !titleCont.trim().isEmpty();
    }

    public boolean hasAssigneeFilter() {
        return assigneeId != null;
    }

    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty();
    }

    public boolean hasLabelFilter() {
        return labelId != null;
    }

    public boolean hasAnyFilter() {
        return hasTitleFilter() || hasAssigneeFilter() || hasStatusFilter() || hasLabelFilter();
    }
}
