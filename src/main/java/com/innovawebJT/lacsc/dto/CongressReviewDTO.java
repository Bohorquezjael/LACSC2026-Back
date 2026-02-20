package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.ReviewType;
import com.innovawebJT.lacsc.enums.Status;

public record CongressReviewDTO(
		Status status,
		String message,
		ReviewType type
) {
}
