package pl.projekt.backend.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pl.projekt.backend.dto.ChangeRoleRequest;
import pl.projekt.backend.repository.UserRepository;
import pl.projekt.backend.model.Role;
import pl.projekt.backend.model.User;
import java.util.List;
import java.util.stream.Collectors;
import pl.projekt.backend.dto.UserResponse;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    
    public void changeUserRole(ChangeRoleRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Role role = Role.valueOf(request.getNewRole().toUpperCase());
            if (role == Role.USER || role == Role.MANAGER) {
                user.setRole(role);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid role: " + request.getNewRole());
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getNewRole());
        }
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .collect(Collectors.toList());
    }
}
