package com.innovawebJT.lacsc.service;

import java.util.List;
import java.util.Optional;

import com.innovawebJT.lacsc.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.innovawebJT.lacsc.model.Summary;

public interface ISummaryService {

    Optional<Summary> getById(Long id);

    void deleteSummary(Long id);

    Page<Summary> getAllSummaries(Pageable pageable);

    List<Summary> getSummariesByAuthorId(Long authorId);

    Summary createSummary(Summary summary, Long authorId);

    List<Summary> getMySummaries();

    Summary createForCurrentUser(Summary summary);
}
