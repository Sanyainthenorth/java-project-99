package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RelationshipMapper {

    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    public void mapTaskRelationships(TaskCreateDTO dto, Task task) {
        mapTaskStatus(dto.getStatus(), task);
        mapAssignee(dto.getAssignee_id(), task);
        mapLabels(dto.getTaskLabelIds(), task);
    }

    public void mapTaskRelationships(TaskUpdateDTO dto, Task task) {
        if (dto.getStatus() != null) {
            mapTaskStatus(dto.getStatus(), task);
        }
        if (dto.getAssignee_id() != null) {
            mapAssignee(dto.getAssignee_id(), task);
        }
        if (dto.getTaskLabelIds() != null) {
            mapLabels(dto.getTaskLabelIds(), task);
        }
    }

    private void mapTaskStatus(String statusSlug, Task task) {
        if (statusSlug != null) {
            TaskStatus status = taskStatusRepository.findBySlug(statusSlug)
                                                    .orElseThrow(() -> new ResourceNotFoundException("TaskStatus not found with slug: " + statusSlug));
            task.setTaskStatus(status);
        }
    }

    private void mapAssignee(Long assigneeId, Task task) {
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                                          .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + assigneeId));
            task.setAssignee(assignee);
        }
    }

    private void mapLabels(Set<Long> labelIds, Task task) {
        if (labelIds != null && !labelIds.isEmpty()) {
            Set<Label> labels = new HashSet<>(labelRepository.findAllById(labelIds));
            task.setLabels(labels);
        } else if (labelIds != null) {
            // Если передали пустой Set - очищаем labels
            task.setLabels(Collections.emptySet());
        }
    }
}
