package com.innovawebJT.lacsc.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.innovawebJT.lacsc.enums.Status;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_enrollments",
		uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private User user;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id")
	@JsonBackReference
	private Course course;

	private String referencePaymentFile;

	@Enumerated(EnumType.STRING)
	private Status paymentStatus;
}