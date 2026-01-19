package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Institution;
import lombok.Builder;

@Builder
public record UserResponseDTO(Long id,
                              String name,
                              String surname,
                              String email,
                              Category category,
                              Institution institution,
                              Status status
                              //sesion especial?
							) {
}
