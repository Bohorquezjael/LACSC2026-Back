package com.innovawebJT.lacsc.dto;

import lombok.Builder;

@Builder
public record UserResponseDTO(Long id,
                              String name,
                              String surname,
                              String email,
                              String badgeName
							) {
}
