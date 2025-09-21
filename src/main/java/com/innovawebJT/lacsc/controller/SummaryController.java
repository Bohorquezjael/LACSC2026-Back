package com.innovawebJT.lacsc.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.service.ISummaryService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/summaries")
public class SummaryController {
    
    private final ISummaryService summaryService;

    @GetMapping("/all")
    public ResponseEntity<Page<Summary>> allSummaries(Pageable pageable) {
        return ResponseEntity.ok(summaryService.getAllSummaries(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Summary> summaryById(@PathVariable Long id){
        return summaryService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSummary(@PathVariable Long id){
        return summaryService.getById(id)
                .map(summary -> {
                    summaryService.deleteSummary(id);
                    return ResponseEntity.noContent().<Void>build();
                }).orElse(ResponseEntity.notFound().build());
    }
}
