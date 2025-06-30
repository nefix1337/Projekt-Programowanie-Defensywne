package pl.projekt.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import pl.projekt.backend.dto.UserBasicInfo;
import pl.projekt.backend.service.UserService;
import java.util.List;

/**
 * Kontroler REST do obsługi operacji na użytkownikach.
 * Udostępnia endpointy do pobierania informacji o aktualnie zalogowanym użytkowniku
 * oraz do pobierania listy wszystkich użytkowników (dla MANAGERA i ADMINA).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Użytkownik", description = "Endpointy użytkowników")
public class UserController {

    private final UserService userService;

    /**
     * Pobiera podstawowe informacje o aktualnie zalogowanym użytkowniku.
     *
     * @return odpowiedź HTTP 200 z danymi użytkownika
     */
    @Operation(summary = "Pobieranie podstawowych informacji o użytkowniku")
    @GetMapping("/me")
    public ResponseEntity<UserBasicInfo> getCurrentUserBasicInfo() {
        return ResponseEntity.ok(userService.getCurrentUserBasicInfo());
    }

    /**
     * Pobiera listę wszystkich użytkowników.
     * Endpoint dostępny tylko dla ról MANAGER i ADMIN.
     *
     * @return odpowiedź HTTP 200 z listą użytkowników
     */
    @Operation(summary = "Pobieranie listy wszystkich użytkowników")
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<UserBasicInfo>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
