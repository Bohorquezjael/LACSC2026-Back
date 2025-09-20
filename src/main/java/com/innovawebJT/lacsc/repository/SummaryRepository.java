package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.model.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long>, PagingAndSortingRepository<Summary, Long> {
}
