package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.PresentationModality;
import com.innovawebJT.lacsc.enums.SpecialSessions;

import java.time.LocalDate;
import java.util.List;

public record SummaryUpdateRequestDTO (

    String title,
    String abstractDescription,
    SpecialSessions specialSession,
    PresentationModality presentationModality,
    LocalDate presentationDate,
    int presentationRoom,
    List<AuthorRequestDTO> authors
){
}
