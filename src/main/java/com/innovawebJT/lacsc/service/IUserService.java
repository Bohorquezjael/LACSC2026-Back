package com.innovawebJT.lacsc.service;

import com.innovawebJT.lacsc.dto.UserProfileDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserService {

    //User getOrCreateProfile(String keycloakId);

    UserResponseDTO getProfile(String keycloakId);

    //void createProfile(String keycloakId, UserProfileDTO dto);

    UserResponseDTO createOrUpdateProfile(String keycloakId, UserProfileDTO dto);

    Page<UserResponseDTO> getAll(Pageable pageable);

    boolean deleteUser(Long id);

    UserProfileDTO getCurrentUser();


    UserResponseDTO getById(Long id);
}
