package com.innovawebJT.lacsc.service;

import com.innovawebJT.lacsc.dto.SummaryDTO;
import com.innovawebJT.lacsc.dto.SummaryReviewDTO;
import com.innovawebJT.lacsc.dto.SummaryUpdateRequestDTO;
import com.innovawebJT.lacsc.model.Summary;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ISummaryService {

    SummaryDTO create(Summary summary, MultipartFile paymentFile);

    void reuploadPaymentProof(Long summaryId, MultipartFile file);

    Page<SummaryDTO> getAll(Pageable pageable);

    Page<SummaryDTO> getMine(Pageable pageable);

    SummaryDTO getById(Long id);

    Resource getPaymentResource(Long summaryId);

    void delete(Long id);

    SummaryDTO updateInfo(Long id, SummaryUpdateRequestDTO request);

    SummaryDTO reviewSummary(Long id, SummaryReviewDTO review);

    Page<SummaryDTO> getAllByUserId(Long id, Pageable pageable);
}
