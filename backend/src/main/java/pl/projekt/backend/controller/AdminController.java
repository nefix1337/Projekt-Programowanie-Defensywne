package pl.projekt.backend.controller;

import pl.projekt.backend.dto.ChangeRoleRequest;
import pl.projekt.backend.dto.UserResponse;
import pl.projekt.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/change-role")
    public ResponseEntity<String> changeUserRole(@RequestBody ChangeRoleRequest request) {
        adminService.changeUserRole(request);
        return ResponseEntity.ok("User role updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }
    
}

