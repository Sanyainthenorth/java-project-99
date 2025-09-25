package hexlet.code.integration;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTUtils jwtUtils;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        authToken = jwtUtils.generateToken(testUser.getEmail());
    }

    @Test
    void shouldCreateUserWithoutAuthentication() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("new@example.com");
        userCreateDTO.setFirstName("Jane");
        userCreateDTO.setLastName("Smith");
        userCreateDTO.setPassword("newpassword123");

        mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void shouldAuthenticateAndGetToken() throws Exception {
        String authRequest = """
            {
                "username": "test@example.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authRequest))
               .andExpect(status().isOk())
               .andExpect(content().string(not(emptyString())));
    }

    // ТЕСТЫ ДЛЯ ЗАЩИЩЕННЫХ ENDPOINTS (требуют аутентификации)

    @Test
    void shouldGetAllUsersWithAuthentication() throws Exception {
        mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void shouldReturnUnauthorizedForGetAllUsersWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateUserWithAuthentication() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("UpdatedName");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.firstName").value("UpdatedName"));
    }

    @Test
    void shouldReturnUnauthorizedForUpdateWithoutToken() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("UpdatedName");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDeleteUserWithAuthentication() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnUnauthorizedForDeleteWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
               .andExpect(status().isUnauthorized());
    }


    @Test
    void shouldReturnBadRequestForDuplicateEmail() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("test@example.com"); // Дублирующий email
        userCreateDTO.setFirstName("Jane");
        userCreateDTO.setLastName("Smith");
        userCreateDTO.setPassword("password123");

        mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
               .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        String authRequest = """
        {
            "username": "test@example.com",
            "password": "wrongpassword"
        }
        """;

        mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authRequest))
               .andExpect(status().isUnauthorized())
               .andExpect(content().string("")); // Пустое тело
    }
    @Test
    void shouldGetUserByIdWithAuthentication() throws Exception {
        // Используем тестового пользователя вместо администратора
        String token = authenticateUser("test@example.com", "password123");

        mockMvc.perform(get("/api/users/{id}", testUser.getId())
                            .header("Authorization", "Bearer " + token))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentUserWithAuthentication() throws Exception {
        String token = authenticateUser("test@example.com", "password123");

        mockMvc.perform(get("/api/users/999")
                            .header("Authorization", "Bearer " + token))
               .andExpect(status().isNotFound());
    }

    // Вспомогательный метод для аутентификации
    private String authenticateUser(String email, String password) throws Exception {
        String authRequest = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", email, password);

        MvcResult result = mockMvc.perform(post("/api/login")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(authRequest))
                                  .andExpect(status().isOk())
                                  .andReturn();

        return result.getResponse().getContentAsString();
    }
    @Test
    void shouldReturnUnauthorizedForGetUserByIdWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
               .andExpect(status().isUnauthorized()); // Ожидаем 401, а не 200
    }

    @Test
    void shouldReturnUnauthorizedForNonExistentUserWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/999"))
               .andExpect(status().isUnauthorized()); // Ожидаем 401, а не 404
    }
}

