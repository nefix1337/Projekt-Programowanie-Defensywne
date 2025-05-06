package pl.projekt.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.projekt.backend.dto.UserBasicInfo;
import pl.projekt.backend.model.User;
import pl.projekt.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserBasicInfo getCurrentUserBasicInfo() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserBasicInfo(user.getFirstName(), user.getLastName(), user.getEmail());
    }
}
