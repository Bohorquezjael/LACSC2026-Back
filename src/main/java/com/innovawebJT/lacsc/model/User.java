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

	private byte age;

	private char gender;

	private String email;

	private String cellphone;

	private String country;
	
	@Enumerated(EnumType.STRING)
	private Category category;

	private String password;

	private String badgeName;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL) 
	private EmergencyContact emergencyContact;
	
	@Embedded
	private Institution institution;
	
	private String referencePaymentFile;
	
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "author")
    private List<Summary> summaryAsAuthor;
	
	@CreationTimestamp
	private LocalDateTime createdAt;
}
