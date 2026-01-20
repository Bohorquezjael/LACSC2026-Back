package com.innovawebJT.lacsc.controller;

import com.innovawebJT.lacsc.dto.SummaryUpdateRequestDTO;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.service.ISummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

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

    @PutMapping("/{id}")
    public ResponseEntity<Summary> updateInfo(
            @PathVariable Long id,
            @RequestBody SummaryUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(
                summaryService.updateInfo(id, request)
        );
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
    public ResponseEntity<Page<Summary>> all(Pageable pageable) {
        return ResponseEntity.ok(summaryService.getAll(pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<Summary>> mine(Pageable pageable) {
        return ResponseEntity.ok(summaryService.getMine(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Summary> byId(@PathVariable Long id) {
        return ResponseEntity.ok(summaryService.getById(id));
    }

    /* ===================== DELETE ===================== */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        summaryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
