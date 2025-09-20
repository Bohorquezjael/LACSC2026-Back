package com.innovawebJT.lacsc.service;

import java.util.Optional;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.domain.Page;

import com.innovawebJT.lacsc.model.Summary;

public interface ISummaryService {

    Page<Summary> getAllSummaries(Pageable pageable);

    Optional<Summary> getById(Long id);

    void deleteSummary(Long id);
}
