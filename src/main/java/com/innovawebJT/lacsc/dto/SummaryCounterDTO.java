package com.innovawebJT.lacsc.dto;

import lombok.Builder;

@Builder
public record SummaryCounterDTO(
		int summariesPendingForReview,
		int totalOfSummaries
) {
}
