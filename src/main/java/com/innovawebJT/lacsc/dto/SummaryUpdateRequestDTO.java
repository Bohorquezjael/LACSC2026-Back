package com.innovawebJT.lacsc.dto;

import com.innovawebJT.lacsc.enums.PresentationModality;

import java.time.LocalDateTime;
import java.util.List;

public record SummaryUpdateRequestDTO (

    PresentationModality presentationModality,
    LocalDateTime presentationDateTime,
    int presentationRoom,
    List<AuthorRequestDTO> authors
){
}
