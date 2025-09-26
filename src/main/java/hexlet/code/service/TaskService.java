package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskMapper taskMapper;

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                             .map(taskMapper::toDto)
                             .toList();
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                                  .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        return taskMapper.toDto(task);
    }

    public TaskDTO createTask(TaskCreateDTO taskCreateDto) {
        Task task = new Task();
        task.setName(taskCreateDto.getTitle());
        task.setDescription(taskCreateDto.getContent());
        task.setIndex(taskCreateDto.getIndex());

        // TaskStatus - обязательное поле
        TaskStatus taskStatus = taskStatusRepository.findByName(taskCreateDto.getStatus())
                                                    .orElseThrow(() -> new ResourceNotFoundException("TaskStatus not found with name: " + taskCreateDto.getStatus()));

        // Assignee - не обязательное поле
        if (taskCreateDto.getAssignee_id() != null) {
            User assignee = userRepository.findById(taskCreateDto.getAssignee_id())
                                          .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + taskCreateDto.getAssignee_id()));
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);
        return taskMapper.toDto(savedTask);
    }

    public TaskDTO updateTask(Long id, TaskUpdateDTO taskUpdateDto) {
        Task task = taskRepository.findById(id)
                                  .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (taskUpdateDto.getTitle() != null) {
            task.setName(taskUpdateDto.getTitle());
        }
        if (taskUpdateDto.getContent() != null) {
            task.setDescription(taskUpdateDto.getContent());
        }
        if (taskUpdateDto.getIndex() != null) {
            task.setIndex(taskUpdateDto.getIndex());
        }
        if (taskUpdateDto.getStatus() != null) {
            TaskStatus taskStatus = taskStatusRepository.findByName(taskUpdateDto.getStatus())
                                                        .orElseThrow(() -> new ResourceNotFoundException("TaskStatus not found with name: " + taskUpdateDto.getStatus()));
            task.setTaskStatus(taskStatus);
        }
        if (taskUpdateDto.getAssignee_id() != null) {
            User assignee = userRepository.findById(taskUpdateDto.getAssignee_id())
                                          .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + taskUpdateDto.getAssignee_id()));
            task.setAssignee(assignee);
        }

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toDto(updatedTask);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
}
