package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.UserCreateDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.UserRepository;
import com.innovawebJT.lacsc.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements IUserService {

	private final UserRepository repository;

	@Override
	public UserResponseDTO create(UserCreateDTO dto) {
		User user = User.builder()
				.name(dto.name())
				.surname(dto.surname())
				.email(dto.email())
				.badgeName(dto.badgeName())
				.category(dto.category())
				.institution(dto.institution())
				.build();
		if(repository.existsByEmail(user.getEmail()) || repository.existsByEmailAndNameAndSurnameAndBadgeName(
				user.getEmail(), user.getName(), user.getSurname(), user.getBadgeName()
		)){
			throw new RuntimeException("User with name " + dto.name() + " already exists");
		}
		User savedUser = repository.save(user);

		return UserResponseDTO.builder()
				.id(savedUser.getId())
				.name(savedUser.getName())
				.surname(savedUser.getSurname())
				.email(savedUser.getEmail())
				.badgeName(savedUser.getBadgeName())
				.build();
	}

}
