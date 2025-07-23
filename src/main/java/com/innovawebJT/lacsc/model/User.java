package com.innovawebJT.lacsc.model;

import com.innovawebJT.lacsc.enums.Category;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
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
	@Embedded
	private Institution institution;

	//private String referencePay;
	//private String referenceFile;
}
