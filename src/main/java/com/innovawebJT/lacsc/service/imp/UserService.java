package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.UserCreateDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.exception.UserNotFoundException;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.UserRepository;
import com.innovawebJT.lacsc.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
		if(repository.existsByEmail(user.getEmail())){
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

	@Override
	public User get(Long id) {
		return repository.findById(id)
						.orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
	}

	@Override
	public Page<UserResponseDTO> getAll(Pageable pageable) {
		return repository.findAllUsersSummary(pageable);
	}

	@Override
	public UserResponseDTO getByEmail(String email) {
		User user = repository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
		return UserResponseDTO.builder()
				.id(user.getId())
				.name(user.getName())
				.surname(user.getSurname())
				.email(user.getEmail())
				.badgeName(user.getBadgeName())
				.build();
	}

	@Override
	public boolean deleteUser(Long id) {
		if (repository.existsById(id)) {
			repository.deleteById(id);
			return true;
		} else {
			return false;
		}
	}
}
