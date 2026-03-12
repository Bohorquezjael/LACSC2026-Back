package com.innovawebJT.lacsc.dto;

import lombok.Builder;

@Builder
public record AuthorDTO(
		Long id,
		String name,
		String email,
		String institutionName,
		int authorOrder
) {}