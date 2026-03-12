package com.innovawebJT.lacsc.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

	private final AuditLogger auditLogger;
	private final HttpServletRequest request;

	@AfterReturning("@annotation(audit)")
	public void logAction(JoinPoint joinPoint, Audit audit) {

		Object[] args = joinPoint.getArgs();

		String entityId = null;

		if (args.length > 0 && args[0] != null) {
			entityId = args[0].toString();
		}

		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isBlank()) {
			ip = request.getRemoteAddr();
		}

		String userAgent = request.getHeader("User-Agent");

		auditLogger.log(
				audit.action(),
				audit.entity(),
				entityId,
				ip,
				userAgent
		);
	}
}