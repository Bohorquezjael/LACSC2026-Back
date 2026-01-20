package com.innovawebJT.lacsc.model;

import com.innovawebJT.lacsc.enums.PresentationModality;
import com.innovawebJT.lacsc.enums.SpecialSessions;
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
@Table(name="summaries")
public class Summary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String abstractDescription;

    @Enumerated(EnumType.STRING)
    SpecialSessions specialSession;

    @Enumerated(EnumType.STRING)
    PresentationModality presentationModality;

    @Enumerated(EnumType.STRING)
    private Status summaryPayment;

     @OneToMany(
        mappedBy = "summary",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Author> authors;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "presenter_user_id")
    private User presenter;

    private LocalDate presentationDate;

    private int presentationRoom;

    //la asignamos conforme la sala etc... unicamente se manda para el correo
    private String keyAbstract;

    private String referencePaymentFile;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
