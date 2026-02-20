package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.SummaryReviewDTO;
import com.innovawebJT.lacsc.dto.SummaryUpdateRequestDTO;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.service.ISummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final ISummaryService summaryService;

    /* ===================== CREATE ===================== */

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Summary> create(
            @RequestPart("summary") Summary summary,
            @RequestPart("paymentFile") MultipartFile paymentFile
    ) {
        Summary created = summaryService.create(summary, paymentFile);
        return ResponseEntity
                .created(URI.create("/api/summaries/" + created.getId()))
                .body(created);
    }

    /* ===================== UPDATE INFO (SIN PDF) ===================== */

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_GENERAL', 'ADMIN_SESSION')")
    public ResponseEntity<Summary> updateInfo(
            @PathVariable Long id,
            @RequestBody SummaryUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(
                summaryService.updateInfo(id, request)
        );
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN_GENERAL', 'ADMIN_SESSION')")
    public ResponseEntity<Summary> review(
            @PathVariable Long id,
            @RequestBody SummaryReviewDTO review
    ) {
        return ResponseEntity.ok(summaryService.reviewSummary(id, review));
    }

    /* ===================== REUPLOAD PDF ===================== */

    @PutMapping(
        value = "/{id}/payment-proof",
        consumes = "multipart/form-data"
    )
    public ResponseEntity<Void> reuploadPaymentProof(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        summaryService.reuploadPaymentProof(id, file);
        return ResponseEntity.noContent().build();
    }

    /* ===================== GETS ===================== */

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN_GENERAL', 'ADMIN_SESSION', 'ADMIN_PAGOS', 'ADMIN_REVISION')")
    public ResponseEntity<Page<Summary>> all(Pageable pageable) {
        return ResponseEntity.ok(summaryService.getAll(pageable));
    }

    @GetMapping("{userId}/all")
    @PreAuthorize("hasAnyRole('ADMIN_GENERAL', 'ADMIN_SESSION', 'ADMIN_PAGOS', 'ADMIN_REVISION')")
    public ResponseEntity<List<Summary>> allByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(summaryService.getAllByUserId(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<Summary>> mine(Pageable pageable) {
        return ResponseEntity.ok(summaryService.getMine(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_GENERAL', 'ADMIN_SESSION', 'ADMIN_PAGOS', 'ADMIN_REVISION')")
    public ResponseEntity<Summary> byId(@PathVariable Long id) {
        return ResponseEntity.ok(summaryService.getById(id));
    }

    @GetMapping("/{id}/payment-file")
    public ResponseEntity<Resource> getPaymentFile(@PathVariable Long id) {
        Summary summary = summaryService.getById(id);

        Resource file = summaryService.getPaymentResource(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /* ===================== DELETE ===================== */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_GENERAL')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        summaryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
