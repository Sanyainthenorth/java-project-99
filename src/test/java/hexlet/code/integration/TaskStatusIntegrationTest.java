package hexlet.code.integration;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTUtils jwtUtils;

    private TaskStatus testStatus;
    private String authToken;

    @BeforeEach
    void setUp() {
        taskStatusRepository.deleteAll();

        testStatus = new TaskStatus();
        testStatus.setName("Test Status");
        testStatus.setSlug("test_status");
        testStatus.setCreatedAt(LocalDateTime.now());
        testStatus = taskStatusRepository.save(testStatus);

        // Генерируем токен для аутентификации
        authToken = jwtUtils.generateToken("hexlet@example.com");
    }

    // ===== ТЕСТЫ НА АУТЕНТИФИКАЦИЮ =====

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
        updateDTO.setName("Updated Status");

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

    // ===== ТЕСТЫ НА УСПЕШНЫЕ ОПЕРАЦИИ =====

    @Test
    void shouldGetAllStatusesWithAuthentication() throws Exception {
        // Создаем еще один статус для теста
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
        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName("Updated Status Name");
        updateDTO.setSlug("updated_slug");

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
        // Обновляем только имя
        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName("Only Name Updated");

        mockMvc.perform(put("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(testStatus.getId()))
               .andExpect(jsonPath("$.name").value("Only Name Updated"))
               .andExpect(jsonPath("$.slug").value(testStatus.getSlug())); // slug остался прежним

        // Обновляем только slug
        TaskStatusUpdateDTO updateDTO2 = new TaskStatusUpdateDTO();
        updateDTO2.setSlug("only_slug_updated");

        mockMvc.perform(put("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO2)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Only Name Updated")) // имя осталось прежним
               .andExpect(jsonPath("$.slug").value("only_slug_updated"));
    }

    @Test
    void shouldDeleteStatusWithAuthentication() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNoContent());

        // Проверяем что статус действительно удален
        mockMvc.perform(get("/api/task_statuses/{id}", testStatus.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNotFound());
    }

    // ===== ТЕСТЫ НА ВАЛИДАЦИЮ =====

    @Test
    void shouldReturnBadRequestForInvalidCreateData() throws Exception {
        // Пустое имя
        TaskStatusCreateDTO invalidDTO = new TaskStatusCreateDTO();
        invalidDTO.setName("");
        invalidDTO.setSlug("valid_slug");

        mockMvc.perform(post("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
               .andExpect(status().isBadRequest());

        // Пустой slug
        TaskStatusCreateDTO invalidDTO2 = new TaskStatusCreateDTO();
        invalidDTO2.setName("Valid Name");
        invalidDTO2.setSlug("");

        mockMvc.perform(post("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO2)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnEmptyListWhenNoStatuses() throws Exception {
        taskStatusRepository.deleteAll();

        mockMvc.perform(get("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(0)));
    }

    // Вспомогательный метод для аутентификации (если нужно тестировать с разными пользователями)
    private String authenticateUser(String email) throws Exception {
        return jwtUtils.generateToken(email);
    }
}

