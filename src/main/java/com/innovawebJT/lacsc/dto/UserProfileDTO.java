package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.model.Institution;
import lombok.Builder;

@Builder
public record UserProfileDTO (
		String name,
		String surname,
		String cellphone,
		char gender,
		String email,
		String country,
		byte age,
		String badgeName,
        Category category,
        Institution institution,
		EmergencyContactDTO emergencyContact
){
}
