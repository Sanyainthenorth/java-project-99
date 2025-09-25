package hexlet.code.component;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskStatusRepository taskStatusRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createAdminUser();
        createDefaultTaskStatuses();
    }

    private void createAdminUser() {
        String email = "hexlet@example.com";

        if (!userRepository.existsByEmail(email)) {
            User admin = new User();
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode("qwerty"));
            admin.setRole(User.Role.ADMIN);
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
            System.out.println("Admin user created: " + email);
        }
    }

    private void createDefaultTaskStatuses() {
        List<TaskStatus> defaultStatuses = List.of(
            new TaskStatus("Draft", "draft"),
            new TaskStatus("To Review", "to_review"),
            new TaskStatus("To Be Fixed", "to_be_fixed"),
            new TaskStatus("To Publish", "to_publish"),
            new TaskStatus("Published", "published")
        );

        for (TaskStatus status : defaultStatuses) {
            if (!taskStatusRepository.existsBySlug(status.getSlug())) {
                taskStatusRepository.save(status);
                System.out.println("Task status created: " + status.getName() + " (" + status.getSlug() + ")");
            }
        }
    }
}