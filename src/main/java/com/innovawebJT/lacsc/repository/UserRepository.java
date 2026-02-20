package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.enums.SpecialSessions;
import com.innovawebJT.lacsc.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKeycloakId(String keycloakId);

    boolean existsByKeycloakId(String keycloakId);

    Optional<User> findByBadgeName(String badgeName);

    @Query("SELECT DISTINCT u FROM Summary s JOIN s.presenter u WHERE s.specialSession IN :sessions")
    Page<User> findUsersBySpecialSessions(@Param("sessions") List<SpecialSessions> sessions, Pageable pageable);

//    @Query("""
//        SELECT new com.innovawebJT.lacsc.dto.UserResponseDTO(
//            u.id,
//            u.name,
//            u.surname,
//            u.email,
//            u.category,
//            u.institution,
//            u.status
//        )
//        FROM User u
//    """)
//    Page<UserResponseDTO> findAllUsers(Pageable pageable);

    Optional<User> findByEmail(String email);
}
