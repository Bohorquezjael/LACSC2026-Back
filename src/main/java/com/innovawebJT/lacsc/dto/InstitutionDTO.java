package com.innovawebJT.lacsc.dto;

import lombok.Builder;

@Builder
public record InstitutionDTO(
		String institutionName,
		String institutionAcronym,
		String institutionCountry
) {
}
