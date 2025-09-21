package hexlet.code.integration;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(testUser.getId()))
               .andExpect(jsonPath("$.email").value("test@example.com"))
               .andExpect(jsonPath("$.firstName").value("John"))
               .andExpect(jsonPath("$.lastName").value("Doe"))
               .andExpect(jsonPath("$.createdAt").exists())
               .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/users/999"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value("User not found with id: 999"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].email").value("test@example.com"))
               .andExpect(jsonPath("$[0].firstName").value("John"))
               .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("new@example.com");
        userCreateDTO.setFirstName("Jane");
        userCreateDTO.setLastName("Smith");
        userCreateDTO.setPassword("newpassword123");

        mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").exists())
               .andExpect(jsonPath("$.email").value("new@example.com"))
               .andExpect(jsonPath("$.firstName").value("Jane"))
               .andExpect(jsonPath("$.lastName").value("Smith"))
               .andExpect(jsonPath("$.createdAt").exists())
               .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestForDuplicateEmail() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("test@example.com"); // Существующий email
        userCreateDTO.setFirstName("Jane");
        userCreateDTO.setLastName("Smith");
        userCreateDTO.setPassword("password123");

        mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("User with email already exists: test@example.com"));
    }

    @Test
    void shouldUpdateUserPartially() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("updated@example.com");
        updateDTO.setFirstName("UpdatedName");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("updated@example.com"))
               .andExpect(jsonPath("$.firstName").value("UpdatedName"))
               .andExpect(jsonPath("$.lastName").value("Doe")) // unchanged
               .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldUpdateUserPassword() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setPassword("newsecurepassword");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("test@example.com")); // email unchanged

        // Проверяем, что пароль действительно изменился в базе
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newsecurepassword", updatedUser.getPassword()));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
               .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("invalid-email");
        userCreateDTO.setPassword("validpassword123");

        mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
               .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForShortPassword() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("valid@example.com");
        userCreateDTO.setPassword("pw"); // меньше 3 символов

        mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userCreateDTO)))
               .andExpect(status().isBadRequest());
    }
}
