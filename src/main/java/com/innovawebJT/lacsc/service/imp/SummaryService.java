package com.innovawebJT.lacsc.service.imp;

import java.util.Optional;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.repository.SummaryRepository;
import com.innovawebJT.lacsc.service.ISummaryService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class SummaryService implements ISummaryService {

    private final SummaryRepository summaryRepository;

    @Override
    public Page<Summary> getAllSummaries(Pageable pageable) {
        return summaryRepository.findAll(pageable);
    }

    @Override
    public Optional<Summary> getById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public void deleteSummary(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteSummary'");
    }
}