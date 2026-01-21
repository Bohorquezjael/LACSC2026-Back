package com.innovawebJT.lacsc.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

	private String email;

	private String institutionName;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id")
	@JsonBackReference
    private Summary summary;

	private int authorOrder;
}