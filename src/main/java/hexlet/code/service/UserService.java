package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.exception.DuplicateEmailException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                             .map(userMapper::toDTO)
                             .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                                  .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new DuplicateEmailException("User with email already exists: " + userCreateDTO.getEmail());
        }

        User user = userMapper.toEntity(userCreateDTO);
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));

        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                                  .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (updateDTO.getEmail() != null) {
            if (!updateDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new DuplicateEmailException("Email already exists: " + updateDTO.getEmail());
            }
            user.setEmail(updateDTO.getEmail());
        }

        if (updateDTO.getFirstName() != null) {
            user.setFirstName(updateDTO.getFirstName());
        }

        if (updateDTO.getLastName() != null) {
            user.setLastName(updateDTO.getLastName());
        }

        if (updateDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
