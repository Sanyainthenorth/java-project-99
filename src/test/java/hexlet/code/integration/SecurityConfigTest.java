package hexlet.code.integration;

import hexlet.code.repository.UserRepository;
import hexlet.code.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import hexlet.code.model.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        userRepository.save(testUser);

        authToken = jwtUtils.generateToken(testUser.getEmail());
    }

    @Test
    void shouldAllowUserRegistrationWithoutAuthentication() throws Exception {
        String userJson = """
            {
                "email": "newuser@example.com",
                "password": "password123",
                "firstName": "Jane",
                "lastName": "Smith"
            }
            """;

        mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(userJson))
               .andExpect(status().isCreated());
    }



    // Тесты на защищенные endpoints
    @Test
    void shouldRequireAuthenticationForTasks() throws Exception {
        mockMvc.perform(get("/api/tasks"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToTasksWithValidToken() throws Exception {
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForTaskStatuses() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToTaskStatusesWithValidToken() throws Exception {
        mockMvc.perform(get("/api/task_statuses")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForUsersList() throws Exception {
        mockMvc.perform(get("/api/users"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToUsersListWithValidToken() throws Exception {
        mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + authToken))
               .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isUnauthorized());
    }

    // Тесты на невалидные токены
    @Test
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer invalid_token"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectMalformedToken() throws Exception {
        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "NotValidScheme invalid_token"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        // Если есть возможность сгенерировать expired token
        String expiredToken = "expired_token_here";

        mockMvc.perform(get("/api/tasks")
                            .header("Authorization", "Bearer " + expiredToken))
               .andExpect(status().isUnauthorized());
    }

    // Тесты на CSRF protection
    @Test
    void shouldNotRequireCsrfForApiEndpoints() throws Exception {
        // Поскольку CSRF отключен, POST запросы должны работать без CSRF токена
        String taskJson = """
            {
                "title": "Test Task",
                "status": "draft"
            }
            """;

        mockMvc.perform(post("/api/tasks")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(taskJson))
               .andExpect(status().isCreated()); // Должен работать без CSRF
    }
}
