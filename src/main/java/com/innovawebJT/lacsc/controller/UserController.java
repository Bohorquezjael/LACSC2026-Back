package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.UserProfileDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.UserRepository;
import com.innovawebJT.lacsc.service.IFileStorageService;
import com.innovawebJT.lacsc.service.ISummaryService;
import com.innovawebJT.lacsc.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final IUserService userService;
	private final UserRepository repository;
    private final IFileStorageService fileStorageService;

    // Obtener MI perfil (desde JWT)
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> me() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

	@GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

	@PreAuthorize("hasRole('admin')")
	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
	}

	@PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Status status,
            @RequestBody String message
    ) {
        userService.reviewUserRegistration(id, status, message);
        return ResponseEntity.noContent().build();
    }

	@GetMapping("/{id}/files/payment")
    public ResponseEntity<Resource> getPaymentFile(@PathVariable Long id) {
        Resource file = userService.getCongressFile(id, "payment");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

	@GetMapping("/{id}/files/student")
    public ResponseEntity<Resource> getStudentFile(@PathVariable Long id) {
        Resource file = userService.getCongressFile(id, "student");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }
}