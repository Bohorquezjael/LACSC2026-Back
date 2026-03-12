package com.innovawebJT.lacsc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "keycloak_id"),
        @UniqueConstraint(columnNames = "badge_name")
    }
)
public class User {

	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String surname;

	private char gender;

	@Column(unique = true)
	private String email;

	private String cellphone;

	private String country;

	@Enumerated(EnumType.STRING)
	private Category category;

	@Column(name = "keycloak_id", nullable = false, unique = true)
    private String keycloakId;

	@Column(name = "badge_name", unique = true)
	private String badgeName;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
	private EmergencyContact emergencyContact;

	@Embedded
	private Institution institution;

	private String referencePaymentFile;

	private String referenceStudentFile;

	@OneToMany(mappedBy = "user")
	@JsonManagedReference("user-enrollments")
	private Set<CourseEnrollment> enrollments;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
