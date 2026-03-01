package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.Status;
import lombok.Builder;

@Builder
public record CourseEnrollmentDTO(
		Long enrollmentId,
		Long courseId,
		Status paymentStatus,
		String referencePaymentFile
) {}