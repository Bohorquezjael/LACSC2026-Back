package com.innovawebJT.lacsc.service;

import com.innovawebJT.lacsc.dto.SummaryUpdateRequestDTO;
import com.innovawebJT.lacsc.model.Summary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ISummaryService {

    Summary create(Summary summary, MultipartFile paymentFile);

    void reuploadPaymentProof(Long summaryId, MultipartFile file);

    Page<Summary> getAll(Pageable pageable);

    Page<Summary> getMine(Pageable pageable);

    Summary getById(Long id);

    void delete(Long id);

    Summary updateInfo(Long id, SummaryUpdateRequestDTO request);

}
