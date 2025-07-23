package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.model.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
	Optional<User> findByBadgeName(String badgeName);
	List<User> findAllByName(String name);
	Optional<User> findByNameAndSurname(String name, String surname);
	boolean existsByEmail(String email);
	boolean existsByEmailAndNameAndSurnameAndBadgeName(String email, String name, String surname, String badgeName);
}
