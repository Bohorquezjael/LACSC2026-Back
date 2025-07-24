package com.innovawebJT.lacsc.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Institution {

	private String institutionName;
	private String institutionAcronym;
	private String institutionCountry;

}
