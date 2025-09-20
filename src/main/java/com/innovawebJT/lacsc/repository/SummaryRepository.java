package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.model.Summary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    List<Summary> getSummaryByAuthorId(Long authorId);
}