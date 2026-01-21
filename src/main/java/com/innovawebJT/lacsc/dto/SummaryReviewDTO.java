package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.Status;

public record SummaryReviewDTO(
		Status status,
        String message
) {
}
