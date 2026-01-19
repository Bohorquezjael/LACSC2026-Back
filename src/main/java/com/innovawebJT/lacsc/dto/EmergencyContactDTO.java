package com.innovawebJT.lacsc.dto;

import lombok.Builder;

@Builder
public record EmergencyContactDTO(
		String fullName,
        String relationship,
        String phone
) {
}
