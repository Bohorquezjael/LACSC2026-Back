package com.innovawebJT.lacsc.service;

import com.innovawebJT.lacsc.dto.SummaryReviewDTO;
import com.innovawebJT.lacsc.dto.SummaryUpdateRequestDTO;
import com.innovawebJT.lacsc.model.Summary;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISummaryService {

    Summary create(Summary summary, MultipartFile paymentFile);

    void reuploadPaymentProof(Long summaryId, MultipartFile file);

    Page<Summary> getAll(Pageable pageable);

    Page<Summary> getMine(Pageable pageable);

    Summary getById(Long id);

    Resource getPaymentResource(Long summaryId);

    void delete(Long id);

    Summary updateInfo(Long id, SummaryUpdateRequestDTO request);

    Summary reviewSummary(Long id, SummaryReviewDTO review);

    List<Summary> getAllByUserId(Long id);
}
