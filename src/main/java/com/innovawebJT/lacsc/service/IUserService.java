package com.innovawebJT.lacsc.service;

import com.innovawebJT.lacsc.dto.UserProfileDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Course;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface IUserService {

    //User getOrCreateProfile(String keycloakId);

    UserResponseDTO getProfile(String keycloakId);

    //void createProfile(String keycloakId, UserProfileDTO dto);

    UserResponseDTO createOrUpdateProfile(String keycloakId, UserProfileDTO dto);

    void validateRegistration(String email, String badgeName);

    Page<UserResponseDTO> getAll(Pageable pageable);

    boolean deleteUser(Long id);

    UserProfileDTO getCurrentUser();

    UserProfileDTO getById(Long id);

	void reviewUserRegistration(Long id, Status status, String message);

    Resource getCongressFile(Long id, String payment);

    void enrollToCongress(MultipartFile paymentFile, MultipartFile studentFile);

    List<Course> getMyCourses();

    Resource getMyCongressFile(String type);

    void enrollCurrentUserToCourse(Long courseId, MultipartFile paymentFile);

    Resource getMyCoursePaymentFile(Long courseId);

    Resource getCoursePaymentFile(Long userId, Long courseId);

    void reviewCoursePayment(Long userId, Long courseId, Status status, String message);

    void reuploadCongressPayment(MultipartFile paymentFile, MultipartFile studentFile);

    void reuploadCoursePayment(Long courseId, MultipartFile paymentFile);

    Page<UserResponseDTO> scholarshipCandidates(Pageable pageable);
}
