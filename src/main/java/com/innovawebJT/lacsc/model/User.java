package com.innovawebJT.lacsc.model;

import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
