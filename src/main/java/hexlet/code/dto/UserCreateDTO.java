package hexlet.code.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Size(min = 3, message = "Password must be at least 3 characters")
    private String password;
}