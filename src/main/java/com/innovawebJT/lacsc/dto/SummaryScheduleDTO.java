package com.innovawebJT.lacsc.dto;

import java.time.LocalDateTime;

public record SummaryScheduleDTO(
		LocalDateTime presentationDateTime,
		String presentationRoom
) {}