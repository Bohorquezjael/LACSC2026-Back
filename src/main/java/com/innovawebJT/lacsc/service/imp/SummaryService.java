package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.SummaryCounterDTO;
import com.innovawebJT.lacsc.dto.SummaryReviewDTO;
import com.innovawebJT.lacsc.dto.SummaryUpdateRequestDTO;
import com.innovawebJT.lacsc.enums.FileCategory;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.SummaryRepository;
import com.innovawebJT.lacsc.service.ISummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryService implements ISummaryService {

    private final SummaryRepository summaryRepository;
    private final FileStorageService fileStorageService;
    private final AuthService authService;
    private final MailSenderNotifications emailService;


    @Override
    public Summary create(Summary summary, MultipartFile paymentFile) {

        User currentUser = authService.getCurrentUser();

        String filePath = fileStorageService.store(
    currentUser.getId(),
    FileCategory.SUMMARY_PAYMENT,
    summary.getTitle(),
    paymentFile
);
        summary.setPresenter(currentUser);
        summary.setReferencePaymentFile(filePath);
        summary.setSummaryPayment(Status.PENDING);

        if (summary.getAuthors() != null) {
            summary.getAuthors().forEach(author -> author.setSummary(summary));
        }
        summary.setSummaryStatus(Status.PENDING);
        Summary savedSummary = summaryRepository.save(summary);
        emailService.sendEmail(savedSummary.getPresenter().getEmail(), "Registro de Resumen Exitoso",
            "Hemos recibido tu resumen: " + savedSummary.getTitle() + ". El pago está en revisión.");
        return savedSummary;
    }

    @Override
    public void reuploadPaymentProof(Long summaryId, MultipartFile file) {

        Summary summary = getById(summaryId);
        User currentUser = authService.getCurrentUser();

        if (!summary.getPresenter().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No puedes modificar este resumen");
        }

        if (summary.getSummaryPayment() != Status.REJECTED) {
            throw new IllegalStateException(
                "Solo puedes reenviar comprobantes rechazados"
            );
        }

        String newPath = fileStorageService.replace(
            summary.getReferencePaymentFile(),
            file
        );

        summary.setReferencePaymentFile(newPath);
        summary.setSummaryPayment(Status.PENDING);

        summaryRepository.save(summary);
    }

    @Override
    public Page<Summary> getAll(Pageable pageable) {
        return summaryRepository.findAll(pageable);
    }

    @Override
    public Page<Summary> getMine(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        return summaryRepository.findByPresenter(currentUser, pageable);
    }

    @Override
    public Summary getById(Long id) {
        return summaryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resumen no encontrado"));
    }

    @Override
    public Resource getPaymentResource(Long id) {
        Summary summary = getById(id);
        boolean isAdmin = hasAdminRole();
        User currentUser = isAdmin ? null : authService.getCurrentUser();

        boolean isOwner = !isAdmin && summary.getPresenter().getId().equals(currentUser.getId());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tienes permiso para ver este comprobante");
        }

        if (summary.getReferencePaymentFile() == null) {
            throw new RuntimeException("Este resumen no tiene un comprobante asociado");
        }

        return fileStorageService.load(summary.getReferencePaymentFile());
    }

    @Override
    public void delete(Long id) {
        Summary summary = getById(id);
        boolean isAdmin = hasAdminRole();
        User currentUser = isAdmin ? null : authService.getCurrentUser();

        boolean isOwner = !isAdmin && summary.getPresenter().getId().equals(currentUser.getId());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No puedes eliminar este resumen");
        }

        fileStorageService.delete(summary.getReferencePaymentFile());
        summaryRepository.delete(summary);
    }

    @Override
public Summary updateInfo(Long id, SummaryUpdateRequestDTO request) {

    Summary summary = getById(id);
//    User currentUser = authService.getCurrentUser();
//
//    if (!summary.getPresenter().getId().equals(currentUser.getId())) {
//        throw new AccessDeniedException("No puedes modificar este resumen");
//    }
//
//    if (summary.getSummaryPayment() == Status.APPROVED) {
//        throw new IllegalStateException(
//            "No se puede modificar un resumen aprobado"
//        );
//    }

    summary.setPresentationModality(request.presentationModality());
    summary.setPresentationDate(request.presentationDate());
    summary.setPresentationRoom(request.presentationRoom());
    Summary savedSummary = summaryRepository.save(summary);

        String userEmail = summary.getPresenter().getEmail();
        String title = summary.getTitle();
        String message = """
                Hola, tu resumen "%s" ha sido programado/actualizado.
                Modalidad: %s
                Fecha: %s
                Sala: %s
                """.formatted(
                title,
                request.presentationModality(),
                request.presentationDate(),
                request.presentationRoom()
        );

        emailService.sendEmail(
                userEmail,
                "Actualización de modalidad/agenda - LACSC 2026",
                message
        );

        return savedSummary;
}

@Override
    public Summary reviewSummary(Long id, SummaryReviewDTO review) {
        Summary summary = getById(id);

        summary.setSummaryPayment(review.status());
        Summary saved = summaryRepository.save(summary);

        // Disparamos el correo con el mensaje personalizado del Admin
        String userEmail = summary.getPresenter().getEmail();
        String title = summary.getTitle();
        String subject = review.status() == Status.APPROVED ?
            "Pago Aprobado - LACSC 2026" :
            "Acción Requerida: Pago de Resumen Rechazado - LACSC 2026";

        String message = review.message().trim().isEmpty() ?
                "Le informamos que el pago correspondiente a su resumen \"" + title + "\" ha sido aceptado.\n A partir de este momento, " +
                        "pasará a la etapa de revisión académica, en la cual será evaluado por el comité de revisores." :
                "Le informamos que el pago correspondiente a su resumen \"" + title + "\" ha sido rechazado. \nMotivo: " + review.message()
                + "\nLe solicitamos actualizar el comprobante de pago directamente en la plataforma.";

        emailService.sendEmail(userEmail, subject, message);

        return saved;
    }

    @Override
    public List<Summary> getAllByUserId(Long id) {
        return summaryRepository.getAllByPresenter_Id(id).orElseGet(List::of);
    }

    private boolean hasAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            log.info(ga.getAuthority());
            if ("ROLE_ADMIN".equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public SummaryCounterDTO getCountOfSummariesByUserId(Long userId) {
        int totalOfSummaries = summaryRepository.countAllByPresenter_Id(userId);
        int summariesPendingForReview = summaryRepository.countAllByPresenter_IdAndSummaryPayment(userId, Status.PENDING);
        int summariesRejected = summaryRepository.countAllByPresenter_IdAndSummaryPayment(userId, Status.REJECTED);
        return SummaryCounterDTO.builder()
                .summariesPendingForReview(summariesRejected + summariesPendingForReview)
                .totalOfSummaries(totalOfSummaries)
                .build();
    }
}
