package com.innovawebJT.lacsc.util;

import com.innovawebJT.lacsc.dto.*;
import com.innovawebJT.lacsc.model.CourseEnrollment;
import com.innovawebJT.lacsc.model.EmergencyContact;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.model.User;

public class Helpers {

	public static SummaryDTO mapToSummaryDTO(Summary summary) {
		return SummaryDTO.builder()
				.id(summary.getId())
				.title(summary.getTitle())
				.abstractDescription(summary.getAbstractDescription())
				.specialSession(summary.getSpecialSession())
				.presentationModality(summary.getPresentationModality())
				.summaryPayment(summary.getSummaryPayment())
				.summaryStatus(summary.getSummaryStatus())
				.authors(summary.getAuthors())
				.presenter(mapToResponseDTO(summary.getPresenter()))
				.presentationDateTime(summary.getPresentationDateTime())
				.presentationRoom(summary.getPresentationRoom())
				.keyAbstract(summary.getKeyAbstract())
				.referencePaymentFile(summary.getReferencePaymentFile())
				.createdAt(summary.getCreatedAt())
				.build();
	}

	public static EmergencyContactDTO mapToResponseContactDTO(EmergencyContact contact) {
		return EmergencyContactDTO.builder()
				.fullName(contact.getName())
				.relationship(contact.getRelationship())
				.phone(contact.getCellphone())
				.build();
	}

	public static UserResponseDTO mapToUserResponseDTO(User user, SummaryCounterDTO summaryCounter, CourseCounterDTO courseCounter) {
		return UserResponseDTO.builder()
				.id(user.getId())
				.name(user.getName())
				.surname(user.getSurname())
				.email(user.getEmail())
				.status(user.getStatus())
				.institution(user.getInstitution())
				.category(user.getCategory())
				.summariesReviewed(summaryCounter)
				.courseReviewed(courseCounter)
				.build();
	}

	public static UserProfileDTO mapToResponseDTO(User user) {
		return UserProfileDTO.builder()
				.name(user.getName())
				.surname(user.getSurname())
				.cellphone(user.getCellphone())
				.gender(user.getGender())
				.country(user.getCountry())
				.badgeName(user.getBadgeName())
				.email(user.getEmail())
				.category(user.getCategory())
				.institution(user.getInstitution())
				.emergencyContact(mapToResponseContactDTO(user.getEmergencyContact()))
				.status(user.getStatus())
				.createdAt(user.getCreatedAt())
				.referencePaymentFile(user.getReferencePaymentFile())
				.referenceStudentFile(user.getReferenceStudentFile())
				.build();
	}

	public static CourseEnrollmentDTO mapToDTO(CourseEnrollment e) {
		return CourseEnrollmentDTO.builder()
				.enrollmentId(e.getId())
				.courseId(e.getCourse().getId())
				.paymentStatus(e.getPaymentStatus())
				.referencePaymentFile(e.getReferencePaymentFile())
				.build();
	}
}
