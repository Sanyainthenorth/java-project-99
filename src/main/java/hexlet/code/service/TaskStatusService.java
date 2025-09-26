package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStatusService {


    private TaskStatusRepository taskStatusRepository;
    private TaskStatusMapper taskStatusMapper;
    private final TaskRepository taskRepository;

    public List<TaskStatusDTO> getAll() {
        var taskStatuses = taskStatusRepository.findAll();
        return taskStatuses.stream()
                           .map(taskStatusMapper::map)
                           .toList();
    }

    public TaskStatusDTO findById(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                                             .orElseThrow(() -> new ResourceNotFoundException("TaskStatus not found with id: " + id));
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO data) {
        var taskStatus = taskStatusMapper.map(data);
        taskStatus = taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO update(Long id, TaskStatusUpdateDTO data) {
        var taskStatus = taskStatusRepository.findById(id)
                                             .orElseThrow(() -> new ResourceNotFoundException("TaskStatus not found with id: " + id));

        taskStatusMapper.update(data, taskStatus);
        taskStatus = taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public void delete(Long id) {
        TaskStatus taskStatus = taskStatusRepository.findById(id)
                                                    .orElseThrow(() -> new ResourceNotFoundException("TaskStatus not found with id: " + id));

        // Проверяем, есть ли задачи с этим статусом
        if (taskRepository.existsByTaskStatusId(id)) {
            throw new IllegalStateException(
                "Cannot delete task status with id " + id + " because there are tasks with this status. " +
                    "Please update or delete the tasks first.");
        }

        taskStatusRepository.delete(taskStatus);
    }

    public TaskStatus getReferenceById(Long id) {
        return taskStatusRepository.getReferenceById(id);
    }
}

