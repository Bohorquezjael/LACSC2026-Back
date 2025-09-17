package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.UserCreateDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.service.imp.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

	private final UserService service;

	@PostMapping
	public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserCreateDTO dto) {
		UserResponseDTO user = service.create(dto);
		URI path = URI.create("/users/" + user.id());
		return ResponseEntity.created(path).body(user);
	}

	@Operation(summary = "Retrieve a user by ID", description = "Use this endpoint only for accessing existing users by their unique ID. NOT intended for search or filtering operations.")
	@GetMapping("/{id}")
	public ResponseEntity<User> getUser(
			@Parameter(description = "ID of the user to retrieve", example = "123", required = true) @PathVariable Long id) {
		return ResponseEntity.ok(service.get(id));
	}

	@GetMapping("/all")
	public ResponseEntity<Page<UserResponseDTO>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(service.getAll(pageable));
	}

	@GetMapping
	public ResponseEntity<UserResponseDTO> getByEmail(@RequestParam(required = false) String email) {
		if (email == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(service.getByEmail(email));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		if (service.deleteUser(id)) {
			return ResponseEntity.ok().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}


	
}
