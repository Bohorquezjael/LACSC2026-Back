package com.innovawebJT.lacsc.dto;

public record TokenResponse(
		String accessToken,
		String refreshToken,
		long expiresIn
		) {
}
