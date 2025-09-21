package hexlet.code.controller;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return userService.createUser(userCreateDTO);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UserUpdateDTO updateDTO) {
        return userService.updateUser(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}