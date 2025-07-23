package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IUserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);
	Optional<User> findByBadgeName(String badgeName);
	List<User> findAllByName(String name);
	Optional<User> findByNameAndSurname(String name, String surname);

}
