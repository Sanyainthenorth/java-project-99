package hexlet.code.repository;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    boolean existsByAssigneeId(Long assigneeId);
    boolean existsByTaskStatusId(Long taskStatusId);
    List<Task> findByLabelsContaining(Label label);

    // Простые методы для фильтрации
    List<Task> findByNameContainingIgnoreCase(String name);
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByTaskStatusName(String statusName);
    List<Task> findByLabelsId(Long labelId);

    // Комбинированные методы
    List<Task> findByNameContainingIgnoreCaseAndAssigneeId(String name, Long assigneeId);
    List<Task> findByNameContainingIgnoreCaseAndTaskStatusName(String name, String statusName);
    List<Task> findByAssigneeIdAndTaskStatusName(Long assigneeId, String statusName);
    List<Task> findByNameContainingIgnoreCaseAndAssigneeIdAndTaskStatusName(String name, Long assigneeId, String statusName);
}
