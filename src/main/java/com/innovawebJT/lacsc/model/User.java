package com.innovawebJT.lacsc.model;

import com.innovawebJT.lacsc.enums.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy =  GenerationType.IDENTITY)
	private long id;
	private String name;
	private String surname;
	private String email;
	//private String password;
	private String badgeName;
	private Category category;
	private Institution institution;

	//private String referencePay;
	//private String referenceFile;
}
