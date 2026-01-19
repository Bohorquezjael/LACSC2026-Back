package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.model.EmergencyContact;
import com.innovawebJT.lacsc.model.Institution;
import lombok.Builder;


@Builder
public record RegisterDTO(String name,
                          String surname,
                          String email,
                          String badgeName,
                          Category category,
                          Institution institution,
                          byte age,
                          char gender,
                          String cellphone,
                          String country,
                          String password,
                          EmergencyContact emergencyContact
							) {
}
