package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.enums.Status;
import lombok.Builder;

@Builder
public record UserResponseDTO(Long id,
                              String name,
                              String surname,
                              String email,
                              Category category,
                              InstitutionDTO institution,
                              Status status,
							  SummaryCounterDTO summariesReviewed,
							  CourseCounterDTO courseReviewed
							) {
}
