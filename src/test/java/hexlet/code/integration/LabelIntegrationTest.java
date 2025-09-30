package hexlet.code.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelDTO;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LabelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Label existingLabel;
    private User testUser;
    private TaskStatus testStatus;

    @BeforeEach
    void setUp() {
        // Очищаем данные перед каждым тестом
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";

        // Создаем тестового пользователя
        testUser = new User();
        testUser.setEmail(uniqueEmail);
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(java.time.LocalDateTime.now());
        testUser.setUpdatedAt(java.time.LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Создаем тестовый статус задачи
        testStatus = new TaskStatus("To Do", "to_do");
        testStatus = taskStatusRepository.save(testStatus);

        // Создаем существующую метку для тестов
        existingLabel = new Label();
        existingLabel.setName("Existing Label");
        existingLabel.setCreatedAt(LocalDate.now());
        existingLabel = labelRepository.save(existingLabel);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createLabel_WithValidData_ShouldReturnCreatedLabel() throws Exception {
        // Given
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName("New Feature");

        // When & Then
        mockMvc.perform(post("/api/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id", notNullValue()))
               .andExpect(jsonPath("$.name", is("New Feature")))
               .andExpect(jsonPath("$.createdAt", notNullValue()));

        // Verify in database
        Optional<Label> savedLabel = labelRepository.findByName("New Feature");
        assertTrue(savedLabel.isPresent());
        assertEquals("New Feature", savedLabel.get().getName());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createLabel_WithDuplicateName_ShouldReturnConflict() throws Exception {
        // Given
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName("Existing Label");

        // When & Then
        mockMvc.perform(post("/api/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").exists());

        List<Label> labelsWithSameName = labelRepository.findAll().stream()
                                                        .filter(label -> label.getName().equals("Existing Label"))
                                                        .toList();
        assertEquals(1, labelsWithSameName.size());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createLabel_WithInvalidName_ShouldReturnBadRequest() throws Exception {
        // Given
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName("ab");

        // When & Then
        mockMvc.perform(post("/api/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getLabel_WithExistingId_ShouldReturnLabel() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/labels/{id}", existingLabel.getId()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(existingLabel.getId().intValue())))
               .andExpect(jsonPath("$.name", is("Existing Label")))
               .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getLabel_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/labels/{id}", 999L))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllLabels_ShouldReturnAllLabels() throws Exception {
        // Given
        Label anotherLabel = new Label();
        anotherLabel.setName("Another Label");
        anotherLabel.setCreatedAt(LocalDate.now());
        labelRepository.save(anotherLabel);

        // When & Then
        mockMvc.perform(get("/api/labels"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].name", is("Existing Label")))
               .andExpect(jsonPath("$[1].name", is("Another Label")))
               .andExpect(header().string("X-Total-Count", "2"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateLabel_WithValidData_ShouldReturnUpdatedLabel() throws Exception {
        // Given
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName("Updated Label");

        // When & Then
        mockMvc.perform(put("/api/labels/{id}", existingLabel.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(existingLabel.getId().intValue())))
               .andExpect(jsonPath("$.name", is("Updated Label")));

        Optional<Label> updatedLabel = labelRepository.findById(existingLabel.getId());
        assertTrue(updatedLabel.isPresent());
        assertEquals("Updated Label", updatedLabel.get().getName());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateLabel_WithDuplicateName_ShouldReturnConflict() throws Exception {
        // Given
        Label secondLabel = new Label();
        secondLabel.setName("Second Label");
        secondLabel.setCreatedAt(LocalDate.now());
        secondLabel = labelRepository.save(secondLabel);

        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName("Existing Label");

        // When & Then
        mockMvc.perform(put("/api/labels/{id}", secondLabel.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateLabel_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName("Updated Label");

        mockMvc.perform(put("/api/labels/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteLabel_WithoutAssociatedTasks_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/labels/{id}", existingLabel.getId()))
               .andExpect(status().isNoContent());

        assertFalse(labelRepository.existsById(existingLabel.getId()));
    }

    @Test
    void accessLabel_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/labels"))
               .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/labels/1"))
               .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/labels"))
               .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/labels/1"))
               .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/labels/1"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createLabel_WithNameAtBoundaries_ShouldWorkCorrectly() throws Exception {
        // Test minimum length (3 characters)
        LabelDTO minLabel = new LabelDTO();
        minLabel.setName("abc");

        mockMvc.perform(post("/api/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(minLabel)))
               .andExpect(status().isCreated());

        // Test long name (should work up to 1000 characters)
        LabelDTO maxLabel = new LabelDTO();
        maxLabel.setName("a".repeat(1000));

        mockMvc.perform(post("/api/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(maxLabel)))
               .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void labelNameCaseSensitivity_ShouldBeHandledCorrectly() throws Exception {
        // Given - создаем метку в нижнем регистре
        Label firstLabel = new Label();
        firstLabel.setName("bug");
        firstLabel.setCreatedAt(LocalDate.now());
        labelRepository.save(firstLabel);

        // When - пытаемся создать метку с тем же именем в верхнем регистре
        LabelDTO secondLabelDTO = new LabelDTO();
        secondLabelDTO.setName("BUG");

        // Then - проверяем успешное создание
        mockMvc.perform(post("/api/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondLabelDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.name", is("BUG")));

        // Проверяем только что созданные метки, игнорируя existingLabel
        assertTrue(labelRepository.findByName("bug").isPresent());
        assertTrue(labelRepository.findByName("BUG").isPresent());

        // Проверяем, что это разные метки (разные ID)
        Label bugLabel = labelRepository.findByName("bug").get();
        Label BUGLabel = labelRepository.findByName("BUG").get();
        assertNotEquals(bugLabel.getId(), BUGLabel.getId());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createLabel_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName("   "); // Пустое имя с пробелами

        mockMvc.perform(post("/api/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createLabel_WithNullName_ShouldReturnBadRequest() throws Exception {
        LabelDTO labelDTO = new LabelDTO();
        // name не устанавливаем - будет null

        mockMvc.perform(post("/api/labels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateLabel_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName("   ");

        mockMvc.perform(put("/api/labels/{id}", existingLabel.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(labelDTO)))
               .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAllLabels_WithNoLabels_ShouldReturnEmptyList() throws Exception {
        // Given - очищаем все метки
        labelRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/labels"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(0)))
               .andExpect(header().string("X-Total-Count", "0"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getLabel_AfterUpdate_ShouldReturnUpdatedData() throws Exception {
        // Given - обновляем метку
        LabelDTO updateDTO = new LabelDTO();
        updateDTO.setName("Updated Label Name");

        mockMvc.perform(put("/api/labels/{id}", existingLabel.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk());

        // When & Then - проверяем что GET возвращает обновленные данные
        mockMvc.perform(get("/api/labels/{id}", existingLabel.getId()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Updated Label Name"));
    }
}
