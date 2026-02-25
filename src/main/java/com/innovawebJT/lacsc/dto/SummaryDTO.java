package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.PresentationModality;
import com.innovawebJT.lacsc.enums.SpecialSessions;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Author;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SummaryDTO(
		Long id,
		String title,
		String abstractDescription,
		SpecialSessions specialSession,
		PresentationModality presentationModality,
		Status summaryPayment,
		Status summaryStatus,
		List<Author> authors,
		UserProfileDTO presenter,
		LocalDateTime presentationDateTime,
		int presentationRoom,
		String keyAbstract,
		String referencePaymentFile,
		LocalDateTime createdAt
) {
}
