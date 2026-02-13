package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.SummaryCounterDTO;
import com.innovawebJT.lacsc.dto.SummaryReviewDTO;
import com.innovawebJT.lacsc.dto.SummaryUpdateRequestDTO;
import com.innovawebJT.lacsc.enums.FileCategory;
import com.innovawebJT.lacsc.enums.SpecialSessions;
import com.innovawebJT.lacsc.enums.ReviewType;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.innovawebJT.lacsc.security.SecurityUtils.*;

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

        if (isAdminGeneral()) {
            log.info("Admin general");
            return summaryRepository.findAll(pageable);
        }

        if (isAdminSesion()) {
            log.info("Admin sesion");
            List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();

            if (allowedSessions.isEmpty()) {
                return Page.empty(pageable);
            }

            return summaryRepository
                    .findBySpecialSessionIn(allowedSessions, pageable);
        }
        log.error("No entra a rol");
        throw new AccessDeniedException("No autorizado");
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

        if (isAdminGeneral()) {
            return updateSummary(summary, review, ReviewType.PAYMENT);
        }

        List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();

        if (allowedSessions.contains(summary.getSpecialSession())) {
            return updateSummary(summary, review, ReviewType.ACADEMIC);
        }

        throw new AccessDeniedException("No autorizado");
    }

    @Override
    public List<Summary> getAllByUserId(Long userId) {
        if (isAdminGeneral()) {
            return summaryRepository.getAllByPresenter_Id(userId).orElseGet(List::of);
        }

        if (isAdminSesion()) {
            List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();
            if (allowedSessions.isEmpty()) {
                return List.of();
            }
            // Filtramos los resúmenes del usuario para que solo vea los de su sesión
            return summaryRepository.findAllByPresenter_IdAndSpecialSessionIn(userId, allowedSessions);
        }

        // Si es el dueño, puede ver todos sus resúmenes
        User currentUser = authService.getCurrentUser();
        if (currentUser.getId().equals(userId)) {
            return summaryRepository.getAllByPresenter_Id(userId).orElseGet(List::of);
        }

        throw new AccessDeniedException("No tienes permiso para ver estos resúmenes");
    }

    private boolean hasAdminRole() {
        return isAdminGeneral() || isAdminSesion();
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
    private Summary updateSummary(
            Summary summary,
            SummaryReviewDTO review,
            ReviewType type
    ) {

        if (type == ReviewType.PAYMENT) {
            summary.setSummaryPayment(review.status());
        } else {
            summary.setSummaryStatus(review.status());
        }

        Summary saved = summaryRepository.save(summary);

        sendReviewNotification(saved, review, type);

        return saved;
    }

    private void sendReviewNotification(
            Summary summary,
            SummaryReviewDTO review,
            ReviewType type
    ) {

        String userEmail = summary.getPresenter().getEmail();
        String title = summary.getTitle();

        boolean approved = review.status() == Status.APPROVED;

        String subject;
        String message;

        if (type == ReviewType.PAYMENT) {
            subject = approved
                    ? "Pago Aprobado - LACSC 2026"
                    : "Acción Requerida: Pago de Resumen Rechazado - LACSC 2026";

            message = approved
                    ? """
                  Le informamos que el pago correspondiente a su resumen "%s" ha sido aceptado.
                  A partir de este momento pasará a la etapa de revisión académica.
                  """.formatted(title)
                    : """
                  Le informamos que el pago correspondiente a su resumen "%s" ha sido rechazado.
                  Motivo: %s
                  Le solicitamos actualizar el comprobante de pago en la plataforma.
                  """.formatted(title, review.message());
        } else {

            subject = approved
                    ? "Resumen Aprobado - LACSC 2026"
                    : "Acción Requerida: Resumen Rechazado - LACSC 2026";

            message = approved
                    ? """
                  Le informamos que su resumen "%s" ha sido aceptado.
                  A partir de este momento pasará a la etapa de revisión académica.
                  """.formatted(title)
                    : """
                  Le informamos que su resumen "%s" ha sido rechazado.
                  Motivo: %s
                  """.formatted(title, review.message());
        }

        emailService.sendEmail(userEmail, subject, message);
    }

}
