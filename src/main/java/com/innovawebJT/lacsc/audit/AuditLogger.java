package com.innovawebJT.lacsc.audit;

import com.innovawebJT.lacsc.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

	private static final Logger log = LoggerFactory.getLogger("AUDIT");

	public void log(String action, String entity, String entityId, String ip, String userAgent) {

		String email = getCurrentUserEmail();
		String userId = getCurrentUserId();

		log.info(
				"USER_EMAIL={} USER_ID={} ACTION={} ENTITY={} ENTITY_ID={} IP={} USER_AGENT=\"{}\"",
				email,
				userId,
				action,
				entity,
				entityId,
				ip,
				userAgent
		);
	}

	private String getCurrentUserEmail() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
			return "anonymous";
		}

		return jwt.getClaimAsString("email");
	}

	private String getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
			return "anonymous";
		}

		return jwt.getSubject(); // sub del JWT
	}

	public void logFailedLogin(String ip, String userAgent) {

		log.warn(
				"LOGIN_FAILED IP={} USER_AGENT=\"{}\"",
				ip,
				userAgent
		);
	}
}