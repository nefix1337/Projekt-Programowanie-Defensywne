package pl.projekt.backend.controller;

import pl.projekt.backend.dto.ChangeRoleRequest;
import pl.projekt.backend.dto.UserResponse;
import pl.projekt.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Kontroler REST do obsługi operacji administracyjnych.
 * Udostępnia endpointy do zmiany roli użytkownika oraz pobierania listy wszystkich użytkowników.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administrator", description = "Endpointy administratora")
public class AdminController {

    private final AdminService adminService;

    /**
     * Zmienia rolę użytkownika na podstawie przesłanego żądania.
     * Endpoint dostępny tylko dla roli ADMIN.
     *
     * @param request obiekt zawierający email użytkownika i nową rolę
     * @return odpowiedź HTTP 200 z komunikatem o sukcesie
     */
    @Operation(summary = "Zmiania roli użytkownika")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/change-role")
    public ResponseEntity<String> changeUserRole(@RequestBody ChangeRoleRequest request) {
        adminService.changeUserRole(request);
        return ResponseEntity.ok("User role updated successfully");
    }

    /**
     * Pobiera listę wszystkich użytkowników.
     * Endpoint dostępny tylko dla roli ADMIN.
     *
     * @return odpowiedź HTTP 200 z listą użytkowników
     */
    @Operation(summary = "Pobieranie wszystkich użytkowników")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }
    
}

