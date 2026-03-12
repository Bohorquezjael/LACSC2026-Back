package com.innovawebJT.lacsc.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
public class Institution {

	private String institutionName;
	private String institutionAcronym;
	private String institutionCountry;

}
