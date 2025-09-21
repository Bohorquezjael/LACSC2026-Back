package com.innovawebJT.lacsc.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.repository.SummaryRepository;
import com.innovawebJT.lacsc.repository.UserRepository;
import com.innovawebJT.lacsc.service.ISummaryService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class SummaryService implements ISummaryService {

    private final SummaryRepository summaryRepository;
    private final UserRepository userRepository;

    @Override
    public Page<Summary> getAllSummaries(Pageable pageable) {
        return summaryRepository.findAll(pageable);
    }

    @Override
    public Optional<Summary> getById(Long id) {
        return summaryRepository.findById(id);
    }

    @Override
    public void deleteSummary(Long id) {
        summaryRepository.deleteById(id);
    }

    @Override
    public List<Summary> getSummariesByAuthorId(Long authorId) {
        return summaryRepository.getSummariesByAuthorId(authorId);
    }

    @Override
    public Summary createSummary(Summary summary, Long authorId) {
        Summary summaryCreated = summary;
        summaryCreated.setAuthor(userRepository.findById(authorId).orElse(null));
        return summaryRepository.save(summaryCreated);
    }
}