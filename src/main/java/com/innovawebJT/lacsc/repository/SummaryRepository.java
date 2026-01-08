package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.model.Summary;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    List<Summary> getSummariesByAuthorId(Long authorId);

    Optional<Summary> getSummaryById(Long id);

    List<Summary> findByAuthor(Object author);

    // List<Summary> getSummariesByCoauthorId(Long coAuthorId); //? name correct??? is possibly, how handle unregistered users??
}