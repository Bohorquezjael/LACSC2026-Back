package com.innovawebJT.lacsc.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.innovawebJT.lacsc.model.Summary;

public interface ISummaryService {

    Optional<Summary> getById(Long id);

    void deleteSummary(Long id);

    Page<Summary> getAllSummaries(Pageable pageable);
}
