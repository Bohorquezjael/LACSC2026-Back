package com.innovawebJT.lacsc.service;

import com.innovawebJT.lacsc.dto.UserCreateDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

	UserResponseDTO create(UserCreateDTO dto);

	User get(Long id);

	Page<UserResponseDTO> getAll(Pageable pageable);
}
