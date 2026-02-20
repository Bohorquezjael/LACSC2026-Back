package com.innovawebJT.lacsc.dto;

import lombok.Builder;

@Builder
public record SummaryCounterDTO(
		int approvedSummaries,
		int totalSummaries
) {
}
