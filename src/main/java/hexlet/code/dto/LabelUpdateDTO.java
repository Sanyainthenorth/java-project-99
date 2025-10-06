package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LabelUpdateDTO {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 1000, message = "Name must be between 1 and 1000 characters")
    private String name;
}
