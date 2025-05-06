package pl.projekt.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import pl.projekt.backend.dto.UserBasicInfo;
import pl.projekt.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Użytkownik", description = "Endpointy użytkowników")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Pobieranie podstawowych informacji o użytkowniku")
    @GetMapping("/me")
    public ResponseEntity<UserBasicInfo> getCurrentUserBasicInfo() {
        return ResponseEntity.ok(userService.getCurrentUserBasicInfo());
    }
}
