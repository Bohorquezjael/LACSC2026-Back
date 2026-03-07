package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
	Optional<CourseEnrollment> findByUserIdAndCourseId(Long userId, Long courseId);

	long countAllByUser_Id(Long userId);

	long countAllByUser_IdAndPaymentStatus(Long userId, Status paymentStatus);
}