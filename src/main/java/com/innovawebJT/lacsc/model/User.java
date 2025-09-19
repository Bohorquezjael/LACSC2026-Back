package com.innovawebJT.lacsc.model;

import com.innovawebJT.lacsc.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="Users")
public class User {

	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String surname;

	private String email;

	private String password;

	private String badgeName;

	@Enumerated(EnumType.STRING)
	private Category category;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@Embedded
	private Institution institution;

	private String referencePaymentFile;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "author")
    private List<Summary> summaryAsAuthor;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "coAuthors")
    private List<Summary> summaryAsCoauthor;
}
