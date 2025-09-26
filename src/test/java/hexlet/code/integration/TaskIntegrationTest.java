package hexlet.code.integration;


import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.JWTUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTUtils jwtUtils;

    private User testUser;
    private TaskStatus testStatus;
    private Task testTask;
    private String authToken;

    private int testCounter = 0;

    @BeforeEach
    void setUp() {
        testCounter++;

        // Очищаем базу в правильном порядке
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        testUser = createTestUser(
            "test" + testCounter + "@example.com",
            "John",
            "Doe"
        );

        // Создаем тестовый статус - ВАЖНО: используем одинаковые name и slug для простоты
        testStatus = createTestStatus(
            "draft", // name
            "draft"  // slug
        );

        // Создаем тестовую задачу
        testTask = createTestTask(
            "Test Task",
            "Test Description",
            1,
            testStatus,
            testUser
        );

        authToken = jwtUtils.generateToken(testUser.getEmail());
    }

    @Test
    void shouldGetAllTasksWithAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].title").value("Test Task"))
               .andExpect(jsonPath("$[0].content").value("Test Description"))
               .andExpect(jsonPath("$[0].status").value("draft")) // Используем реальное name статуса
               .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId()))
               .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void shouldReturnUnauthorizedForGetAllTasksWithoutToken() throws Exception {
        mockMvc.perform(get("/api/tasks"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetTaskByIdWithAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(testTask.getId()))
               .andExpect(jsonPath("$.title").value("Test Task"))
               .andExpect(jsonPath("$.content").value("Test Description"))
               .andExpect(jsonPath("$.status").value("draft"))
               .andExpect(jsonPath("$.assignee_id").value(testUser.getId()));
    }


    @Test
    void shouldReturnUnauthorizedForCreateTaskWithoutToken() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("New Task");
        taskCreateDTO.setStatus("draft");

        mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskCreateDTO)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateTaskWithAuthentication() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle("Updated Task");
        updateDTO.setContent("Updated Description");
        updateDTO.setIndex(99);

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title").value("Updated Task"))
               .andExpect(jsonPath("$.content").value("Updated Description"))
               .andExpect(jsonPath("$.index").value(99))
               .andExpect(jsonPath("$.status").value("draft"))
               .andExpect(jsonPath("$.assignee_id").value(testUser.getId()));
    }

    @Test
    void shouldUpdateTaskStatusWithAuthentication() throws Exception {
        // Создаем новый статус для обновления
        TaskStatus newStatus = createTestStatus("in_progress", "in_progress");

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setStatus("in_progress"); // Используем name статуса

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("in_progress"));
    }

    @Test
    void shouldUpdateTaskAssigneeWithAuthentication() throws Exception {
        // Создаем нового пользователя для назначения
        User newUser = createTestUser("newuser@example.com", "Jane", "Smith");

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setAssignee_id(newUser.getId());

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.assignee_id").value(newUser.getId()));
    }

    @Test
    void shouldReturnUnauthorizedForUpdateTaskWithoutToken() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle("Updated Task");

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForDeleteTaskWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId()))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingTaskWithInvalidData() throws Exception {
        TaskCreateDTO invalidTask = new TaskCreateDTO();
        invalidTask.setTitle(""); // Пустой title - нарушение @Size(min = 1)
        invalidTask.setStatus("draft");

        mockMvc.perform(post("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTask)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingTaskWithoutStatus() throws Exception {
        TaskCreateDTO invalidTask = new TaskCreateDTO();
        invalidTask.setTitle("Valid Title");
        // Не устанавливаем status - нарушение @NotBlank

        mockMvc.perform(post("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTask)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingTaskWithNonExistentStatus() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setStatus("non_existent_status");

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingTaskWithNonExistentAssignee() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setAssignee_id(999L); // Несуществующий пользователь

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isNotFound());
    }
    

    private User createTestUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setCreatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    private TaskStatus createTestStatus(String name, String slug) {
        // Проверяем, существует ли уже статус с таким name
        TaskStatus existingStatus = taskStatusRepository.findByName(name).orElse(null);
        if (existingStatus != null) {
            return existingStatus;
        }

        TaskStatus status = new TaskStatus();
        status.setName(name);
        status.setSlug(slug);
        status.setCreatedAt(java.time.LocalDateTime.now());
        return taskStatusRepository.save(status);
    }

    private Task createTestTask(String name, String description, Integer index,
                                TaskStatus status, User assignee) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setIndex(index);
        task.setTaskStatus(status);
        task.setAssignee(assignee);
        task.setCreatedAt(LocalDate.now());
        return taskRepository.save(task);
    }
}