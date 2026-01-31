package com.innovawebJT.lacsc.model;

import com.innovawebJT.lacsc.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private Status paymentStatus;

	private String referencePaymentFile;

	private Status courseStatus;

	@ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
	private Set<User> users = new HashSet<>();
}
