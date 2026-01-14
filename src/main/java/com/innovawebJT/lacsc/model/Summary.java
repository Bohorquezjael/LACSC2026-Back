package com.innovawebJT.lacsc.model;

import com.innovawebJT.lacsc.enums.PresentationModality;
import com.innovawebJT.lacsc.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="Resumes")
public class Summary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String abstractDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    SpecialSessions specialSession;

    PresentationModality presentationModality;

    private boolean isSummaryPaymentVerified;

    // cambiar por la persona que registra el resumen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    //! cambiar por una lista de autores tipo persona
    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CoAuthor> coAuthors;

    private LocalDate presentationDate;

    private int presentationRoom;

    private Status status;

    //la asignamos conforme la sala etc... unicamente se manda para el correo
    private String keyAbstract;

    private String referencePaymentFile;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
