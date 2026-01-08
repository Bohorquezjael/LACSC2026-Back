package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.service.ISummaryService;
import com.innovawebJT.lacsc.service.IUserService;
import lombok.AllArgsConstructor;
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
    private final ISummaryService summaryService;

    // Obtener MI perfil (desde JWT)
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    // Obtener resúmenes del usuario autenticado
    @GetMapping("/me/summaries")
    public ResponseEntity<List<Summary>> mySummaries() {
        return ResponseEntity.ok(summaryService.getMySummaries());
    }

    // Crear resumen como usuario autenticado
    @PostMapping("/me/summaries")
    public ResponseEntity<Summary> createSummary(@RequestBody Summary summary) {
        Summary created = summaryService.createForCurrentUser(summary);
        return ResponseEntity
                .created(URI.create("/api/users/me/summaries/" + created.getId()))
                .body(created);
    }

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
	}

}