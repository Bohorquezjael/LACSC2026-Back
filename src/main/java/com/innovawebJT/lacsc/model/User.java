package com.innovawebJT.lacsc.model;

import com.innovawebJT.lacsc.enums.Category;
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
@Table(name="Users")
public class User {

	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String surname;
	private String email;
	//private String password;
	private String badgeName;
	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private Category category;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@Embedded
	private Institution institution;
	private String referencePaymentFile;
}
