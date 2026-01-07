package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.model.Institution;
import lombok.Builder;

@Builder
public record UserProfileDTO (
		String badgeName,
        Category category,
        Institution institution
){
}
