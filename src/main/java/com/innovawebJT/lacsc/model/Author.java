package com.innovawebJT.lacsc.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class Author {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String surname;

	private String email;

	private String institutionName;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id")
    private Summary summary;

}