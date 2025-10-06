package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.RelationshipMapper;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository; // ✅ ДОБАВЬ
    private final TaskStatusRepository taskStatusRepository; // ✅ ДОБАВЬ
    private final LabelRepository labelRepository;
    private final TaskMapper taskMapper;
    private final RelationshipMapper relationshipMapper;
    private final TaskSpecification taskSpecification;

    public List<TaskDTO> getAllTasks() {
        return getFilteredTasks(new TaskParamsDTO());
    }

    public List<TaskDTO> getFilteredTasks(TaskParamsDTO params) {
        Specification<Task> spec = taskSpecification.build(params);
        List<Task> tasks = taskRepository.findAll(spec);
        return tasks.stream()
                    .map(taskMapper::toDto)
                    .toList();
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                                  .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.toDto(task);
    }

    public TaskDTO createTask(TaskCreateDTO taskCreateDto) {
        System.out.println("Creating task with status: " + taskCreateDto.getStatus());

        Task task = taskMapper.toEntity(taskCreateDto);
        relationshipMapper.mapTaskRelationships(taskCreateDto, task);

        System.out.println("Task after mapping - name: " + task.getName());
        System.out.println("Task after mapping - status: " + (task.getTaskStatus() != null ? task.getTaskStatus().getSlug() : "NULL"));

        if (task.getIndex() == null) {
            task.setIndex(0);
        }

        Task savedTask = taskRepository.save(task);
        return taskMapper.toDto(savedTask);
    }

    public TaskDTO updateTask(Long id, TaskUpdateDTO taskUpdateDto) {
        Task task = taskRepository.findById(id)
                                  .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        taskMapper.updateEntity(taskUpdateDto, task);
        relationshipMapper.mapTaskRelationships(taskUpdateDto, task);

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toDto(updatedTask);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                                  .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
    }

    public List<TaskDTO> getTasksByLabel(Long labelId) {
        Label label = labelRepository.findById(labelId)
                                     .orElseThrow(() -> new ResourceNotFoundException("Label not found with id: " + labelId));
        return taskRepository.findByLabelsContaining(label).stream()
                             .map(taskMapper::toDto)
                             .toList();
    }
}