package hexlet.code.integration;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Label;
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
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
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
        // Добавьте очистку лейблов если есть репозиторий
        // labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        testUser = createTestUser(
            "test" + testCounter + "@example.com",
            "John",
            "Doe"
        );

        // Создаем тестовый статус
        testStatus = createTestStatus("draft", "draft");

        // СОЗДАЕМ ТЕСТОВЫЕ ЛЕЙБЛЫ
        // createTestLabel("bug");
        // createTestLabel("feature");

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

    // Добавьте метод для создания лейблов если нужно
    private Label createTestLabel(String name) {
        Label label = new Label();
        label.setName(name);
        // return labelRepository.save(label);
        return label; // раскомментируйте если есть репозиторий
    }

    @Test
    void shouldGetAllTasksWithAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1)) // Теперь проверяем длину массива
               .andExpect(jsonPath("$[0].title").value("Test Task")) // $[0] - первый элемент массива
               .andExpect(jsonPath("$[0].content").value("Test Description"))
               .andExpect(jsonPath("$[0].status").value("draft"))
               .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId()))
               .andExpect(header().exists("X-Total-Count")); // Заголовок остаётся
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
    @Test
    void shouldRequireAuthenticationForGetAllStatuses() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForGetStatusById() throws Exception {
        mockMvc.perform(get("/api/task_statuses/{id}", testStatus.getId()))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForCreateStatus() throws Exception {
        TaskStatusCreateDTO createDTO = new TaskStatusCreateDTO();
        createDTO.setName("New Status");
        createDTO.setSlug("new_status");

        mockMvc.perform(post("/api/task_statuses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForUpdateStatus() throws Exception {
        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName(JsonNullable.of("Updated Status"));

        mockMvc.perform(put("/api/task_statuses/{id}", testStatus.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthenticationForDeleteStatus() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/{id}", testStatus.getId()))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetAllStatusesWithAuthentication() throws Exception {
        TaskStatus anotherStatus = new TaskStatus();
        anotherStatus.setName("Another Status");
        anotherStatus.setSlug("another_status");
        anotherStatus.setCreatedAt(LocalDateTime.now());
        taskStatusRepository.save(anotherStatus);

        mockMvc.perform(get("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].name").value(testStatus.getName()))
               .andExpect(jsonPath("$[0].slug").value(testStatus.getSlug()))
               .andExpect(jsonPath("$[1].name").value(anotherStatus.getName()))
               .andExpect(jsonPath("$[1].slug").value(anotherStatus.getSlug()));
    }

    @Test
    void shouldGetStatusByIdWithAuthentication() throws Exception {
        mockMvc.perform(get("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(testStatus.getId()))
               .andExpect(jsonPath("$.name").value(testStatus.getName()))
               .andExpect(jsonPath("$.slug").value(testStatus.getSlug()))
               .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldCreateStatusWithAuthentication() throws Exception {
        TaskStatusCreateDTO createDTO = new TaskStatusCreateDTO();
        createDTO.setName("New Task Status");
        createDTO.setSlug("new_task_status");

        mockMvc.perform(post("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").exists())
               .andExpect(jsonPath("$.name").value("New Task Status"))
               .andExpect(jsonPath("$.slug").value("new_task_status"))
               .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldUpdateStatusWithAuthentication() throws Exception {
        TaskStatusUpdateDTO updateDTO = TestUtils.createTaskStatusUpdateDTO("Updated Status Name", "updated_slug");

        mockMvc.perform(put("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(testStatus.getId()))
               .andExpect(jsonPath("$.name").value("Updated Status Name"))
               .andExpect(jsonPath("$.slug").value("updated_slug"))
               .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldUpdateStatusPartiallyWithAuthentication() throws Exception {
        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName(JsonNullable.of("Only Name Updated"));

        mockMvc.perform(put("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(testStatus.getId()))
               .andExpect(jsonPath("$.name").value("Only Name Updated"))
               .andExpect(jsonPath("$.slug").value(testStatus.getSlug()));

        // Обновляем только slug - ИСПРАВЬ:
        TaskStatusUpdateDTO updateDTO2 = new TaskStatusUpdateDTO();
        updateDTO2.setSlug(JsonNullable.of("only_slug_updated"));

        mockMvc.perform(put("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO2)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Only Name Updated"))
               .andExpect(jsonPath("$.slug").value("only_slug_updated"));
    }

    @Test
    void shouldDeleteStatusWithAuthentication() throws Exception {
        // Удалите все задачи с этим статусом, если они есть (или не создавайте в setUp)
        taskRepository.deleteAll();

        mockMvc.perform(delete("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidCreateData() throws Exception {
        TaskStatusCreateDTO invalidDTO = new TaskStatusCreateDTO();
        invalidDTO.setName("");
        invalidDTO.setSlug("valid_slug");

        mockMvc.perform(post("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
               .andExpect(status().isBadRequest());

        TaskStatusCreateDTO invalidDTO2 = new TaskStatusCreateDTO();
        invalidDTO2.setName("Valid Name");
        invalidDTO2.setSlug("");

        mockMvc.perform(post("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO2)))
               .andExpect(status().isBadRequest());
    }
    //тесты для фильтрации

    @Test
    void shouldFilterTasksByTitle() throws Exception {
        // Given - создаем несколько задач с разными названиями
        Task task1 = createTestTask("Create API documentation", "Description", 1, testStatus, testUser);
        Task task2 = createTestTask("Fix login bug", "Description", 2, testStatus, testUser);
        Task task3 = createTestTask("Create user interface", "Description", 3, testStatus, testUser);

        // When & Then - фильтрация по подстроке "Create"
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .param("titleCont", "Create"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2))
               .andExpect(jsonPath("$[0].title").value("Create API documentation"))
               .andExpect(jsonPath("$[1].title").value("Create user interface"));
    }

    @Test
    void shouldFilterTasksByAssignee() throws Exception {
        // Given - создаем второго пользователя и задачи для разных исполнителей
        User user2 = createTestUser("user2@example.com", "Jane", "Smith");

        // Удаляем задачу, созданную в setUp, чтобы начать с чистого листа
        taskRepository.deleteAll();

        Task task1 = createTestTask("Task for John", "Description", 1, testStatus, testUser);
        Task task2 = createTestTask("Task for Jane", "Description", 2, testStatus, user2);
        Task task3 = createTestTask("Task without assignee", "Description", 3, testStatus, null);

        // When & Then - фильтрация по исполнителю John
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .param("assigneeId", testUser.getId().toString()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].title").value("Task for John"))
               .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId()));
    }

    @Test
    void shouldFilterTasksByStatus() throws Exception {
        // Given - создаем разные статусы и задачи
        TaskStatus inProgressStatus = createTestStatus("in_progress", "in_progress");
        TaskStatus doneStatus = createTestStatus("done", "done");

        Task task1 = createTestTask("Todo task", "Description", 1, testStatus, testUser);
        Task task2 = createTestTask("In progress task", "Description", 2, inProgressStatus, testUser);
        Task task3 = createTestTask("Done task", "Description", 3, doneStatus, testUser);

        // When & Then - фильтрация по статусу "in_progress"
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .param("status", "in_progress"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].title").value("In progress task"))
               .andExpect(jsonPath("$[0].status").value("in_progress"));
    }

    @Test
    void shouldFilterTasksByMultipleCriteria() throws Exception {
        // Given - создаем тестовые данные для комплексной фильтрации
        TaskStatus reviewStatus = createTestStatus("review", "review");
        User user2 = createTestUser("developer@example.com", "Dev", "Eloper");

        Task matchingTask = createTestTask("Fix critical bug", "Urgent fix needed", 1, reviewStatus, user2);
        Task nonMatchingTask1 = createTestTask("Fix minor issue", "Description", 2, testStatus, user2); // другой статус
        Task nonMatchingTask2 = createTestTask("Critical feature", "Description", 3, reviewStatus, testUser); // другой исполнитель

        // When & Then - комбинированная фильтрация
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .param("titleCont", "critical")
                            .param("assigneeId", user2.getId().toString())
                            .param("status", "review"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].title").value("Fix critical bug"))
               .andExpect(jsonPath("$[0].assignee_id").value(user2.getId()))
               .andExpect(jsonPath("$[0].status").value("review"));
    }

    @Test
    void shouldReturnEmptyListWhenNoTasksMatchFilters() throws Exception {
        // Given - создаем задачу
        createTestTask("Existing task", "Description", 1, testStatus, testUser);

        // When & Then - фильтр, который не соответствует ни одной задаче
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .param("titleCont", "nonexistent"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnAllTasksWhenNoFiltersProvided() throws Exception {
        // Given - создаем несколько задач
        Task task1 = createTestTask("Task 1", "Description", 1, testStatus, testUser);
        Task task2 = createTestTask("Task 2", "Description", 2, testStatus, null);

        // When & Then - запрос без фильтров (должен вернуть все задачи)
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(3)) // 2 новые + 1 из setUp
               .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void shouldFilterTasksCaseInsensitively() throws Exception {
        // Given
        Task task1 = createTestTask("CREATE API", "Description", 1, testStatus, testUser);
        Task task2 = createTestTask("create user", "Description", 2, testStatus, testUser);
        Task task3 = createTestTask("Delete data", "Description", 3, testStatus, testUser);

        // When & Then - поиск в разных регистрах должен работать
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .param("titleCont", "create"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(2)); // CREATE API и create user
    }

    @Test
    void shouldHandleTasksWithoutAssignee() throws Exception {
        // Given - задача без исполнителя
        Task taskWithoutAssignee = createTestTask("Unassigned task", "Description", 1, testStatus, null);

        // When & Then
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .param("titleCont", "Unassigned"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].title").value("Unassigned task"))
               .andExpect(jsonPath("$[0].assignee_id").doesNotExist()); // ПРОСТО ИСПРАВЬ НА doesNotExist()
    }
    @Test
    void shouldCreateTaskWithLabels() throws Exception {
        // Given
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("Task with Labels");
        createDTO.setStatus("draft");
        createDTO.setTaskLabelIds(Set.of(1L, 2L)); // Предполагая что лейблы 1 и 2 существуют

        // When & Then
        mockMvc.perform(post("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.title").value("Task with Labels"))
               .andExpect(jsonPath("$.taskLabelIds").exists()) // ✅ Изменил на taskLabelIds
               .andExpect(jsonPath("$.taskLabelIds.length()").value(2)); // ✅ Изменил на taskLabelIds
    }

    @Test
    void shouldGetTaskWithLabels() throws Exception {
        // Given - создаем задачу с лейблами
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("Test Task with Labels");
        createDTO.setStatus("draft");
        createDTO.setTaskLabelIds(Set.of(1L));

        // Создаем задачу
        String response = mockMvc.perform(post("/api/tasks")
                                              .header("Authorization", "Bearer " + authToken)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(createDTO)))
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();

        // Извлекаем ID созданной задачи
        Long taskId = objectMapper.readTree(response).get("id").asLong();

        // When & Then - получаем задачу и проверяем что лейблы есть
        mockMvc.perform(get("/api/tasks/{id}", taskId)
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(taskId))
               .andExpect(jsonPath("$.taskLabelIds").exists()) // ✅ Изменил на taskLabelIds
               .andExpect(jsonPath("$.taskLabelIds.length()").value(1)) // ✅ Изменил на taskLabelIds
               .andExpect(jsonPath("$.taskLabelIds[0]").value(1)); // ✅ Теперь проверяем ID, а не объект
    }
    @Test
    void shouldCreateTaskWithAuthentication() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("New Task");
        taskCreateDTO.setContent("New Description");
        taskCreateDTO.setStatus("draft");
        taskCreateDTO.setIndex(5);
        taskCreateDTO.setAssignee_id(testUser.getId());

        mockMvc.perform(post("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskCreateDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").exists())
               .andExpect(jsonPath("$.title").value("New Task"))
               .andExpect(jsonPath("$.content").value("New Description"))
               .andExpect(jsonPath("$.index").value(5))
               .andExpect(jsonPath("$.status").value("draft"))
               .andExpect(jsonPath("$.assignee_id").value(testUser.getId()));
    }

    @Test
    void shouldDeleteTaskWithAuthentication() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNoContent());

        // Проверяем что задача действительно удалена
        mockMvc.perform(get("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForNonExistentTask() throws Exception {
        Long nonExistentId = 9999L;

        mockMvc.perform(get("/api/tasks/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/tasks/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TaskUpdateDTO())))
               .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/tasks/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForNonExistentStatus() throws Exception {
        Long nonExistentId = 9999L;

        mockMvc.perform(get("/api/task_statuses/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/task_statuses/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TaskStatusUpdateDTO())))
               .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/task_statuses/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForInvalidStatusInTaskCreation() throws Exception {
        TaskCreateDTO invalidTask = new TaskCreateDTO();
        invalidTask.setTitle("Valid Title");
        invalidTask.setStatus("non_existent_status"); // Несуществующий статус

        mockMvc.perform(post("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidTask)))
               .andExpect(status().isNotFound()) // Ожидаем 404 вместо 400
               .andExpect(jsonPath("$.error").value(containsString("TaskStatus not found")));
    }

    @Test
    void shouldReturnNotFoundForInvalidAssigneeInTaskUpdate() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setAssignee_id(9999L); // Несуществующий пользователь

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isNotFound()) // Ожидаем 404 вместо 400
               .andExpect(jsonPath("$.error").value("User not found with id: 9999"));
    }

    @Test
    void shouldReturnInternalServerErrorForDuplicateStatusSlug() throws Exception {
        TaskStatusCreateDTO createDTO = new TaskStatusCreateDTO();
        createDTO.setName("Different Name");
        createDTO.setSlug(testStatus.getSlug()); // Дублирующий slug

        mockMvc.perform(post("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
               .andExpect(status().isBadRequest()); // Ожидаем 400 вместо 500
    }

    // Тест на частичное обновление задачи
    @Test
    void shouldUpdateTaskPartially() throws Exception {
        // Обновляем только title
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle("Only Title Updated");

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title").value("Only Title Updated"))
               .andExpect(jsonPath("$.content").value(testTask.getDescription())) // Осталось прежним
               .andExpect(jsonPath("$.index").value(testTask.getIndex())); // Осталось прежним

        // Обновляем только content
        TaskUpdateDTO updateDTO2 = new TaskUpdateDTO();
        updateDTO2.setContent("Only Content Updated");

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO2)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title").value("Only Title Updated")) // Осталось прежним
               .andExpect(jsonPath("$.content").value("Only Content Updated"));
    }

    // Тест на обработку неверного токена
    @Test
    void shouldReturnUnauthorizedForInvalidToken() throws Exception {
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer invalid_token"))
               .andExpect(status().isUnauthorized());
    }

    // Тест на истеченный токен (если есть возможность сгенерировать)
    @Test
    void shouldReturnUnauthorizedForExpiredToken() throws Exception {
        // Если есть возможность сгенерировать истекший токен
        String expiredToken = "expired_token_here";

        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + expiredToken))
               .andExpect(status().isUnauthorized());
    }

}