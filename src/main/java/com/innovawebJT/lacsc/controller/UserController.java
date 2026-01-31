package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.UserProfileDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Course;
import com.innovawebJT.lacsc.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final IUserService userService;

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
	public ResponseEntity<UserProfileDTO> getUser(@PathVariable Long id) {
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

	@GetMapping("/me/courses")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<Course>> myCourses() {
		return ResponseEntity.ok(userService.getMyCourses());
	}

	@GetMapping("/me/files/payment")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Resource> getMyPaymentFile() {
		Resource file = userService.getMyCongressFile("payment");
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.body(file);
	}

	@GetMapping("/me/files/student")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Resource> getMyStudentFile() {
		Resource file = userService.getMyCongressFile("student");
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.body(file);
	}

	@PostMapping(
			value = "/me/course-enroll/{courseId}",
			consumes = "multipart/form-data"
	)
	public ResponseEntity<Void> enrollToCourse(
			@PathVariable Long courseId,
			@RequestPart("paymentFile") MultipartFile paymentFile
	) {
		userService.enrollCurrentUserToCourse(courseId, paymentFile);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me/course-files/{courseId}/payment")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Resource> getMyCoursePaymentFile(@PathVariable Long courseId) {
		Resource file = userService.getMyCoursePaymentFile(courseId);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.body(file);
	}

	@PostMapping(
			value = "/me/congress-enroll",
			consumes = "multipart/form-data"
	)
	public ResponseEntity<Void> uploadCongressFiles(
			@RequestPart("paymentFile") MultipartFile paymentFile,
			@RequestPart(value = "studentFile", required = false) MultipartFile studentFile
	) {
		userService.enrollToCongress(paymentFile, studentFile);
		return ResponseEntity.noContent().build();
	}
}