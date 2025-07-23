package com.innovawebJT.lacsc.service;

import com.innovawebJT.lacsc.dto.UserCreateDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.model.User;

public interface IUserService {

	UserResponseDTO create(UserCreateDTO dto);
}
