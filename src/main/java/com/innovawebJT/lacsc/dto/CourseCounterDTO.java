package com.innovawebJT.lacsc.dto;

import lombok.Builder;

@Builder
public record CourseCounterDTO(
		long approvedCourses,
		long totalCourses
){}