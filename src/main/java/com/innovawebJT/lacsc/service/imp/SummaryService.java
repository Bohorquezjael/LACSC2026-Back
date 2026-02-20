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

        if (isAdminGeneral() || isAdminPagos() || isAdminRevision()) {
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
        Summary summary = summaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Resumen no encontrado"));
        if(!isAdminGeneral() && !isAdminRevision() && !isAdminPagos() && !summary.getPresenter().getId().equals(authService.getCurrentUser().getId())) {
            throw new AccessDeniedException("No tienes permiso para ver este resumen");
        }
        if(isAdminSesion() && !getAllowedSessionsFromRoles().contains(summary.getSpecialSession())) {
            throw new AccessDeniedException("No estas autorizado para ver resumenes de otras sesiones.");
        }
        return summary;
    }

    @Override
    public Resource getPaymentResource(Long id) {
        Summary summary = getById(id);
        User currentUser = isAdminGeneral() || isAdminPagos() || isAdminRevision() ? null : authService.getCurrentUser();

        boolean isOwner = !isAdminGeneral() && !isAdminRevision() && !isAdminPagos() && summary.getPresenter().getId().equals(currentUser.getId());
        if (!isOwner && !isAdminGeneral()) {
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
        String userEmail = summary.getPresenter().getEmail();
        String title = summary.getTitle();
        String message;
        Summary savedSummary;

        if (isAdminGeneral()) {
            summary.setPresentationModality(request.presentationModality());
            summary.setPresentationDateTime(request.presentationDateTime());
            summary.setPresentationRoom(request.presentationRoom());
            savedSummary = summaryRepository.save(summary);

            message = """
                    Hola, tu resumen "%s" ha sido programado/actualizado.
                    Modalidad: %s
                    Fecha: %s
                    Hora: %s
                    Sala: %s
                    """.formatted(
                    title,
                    request.presentationModality(),
                    request.presentationDateTime().toLocalDate(),
                    request.presentationDateTime().toLocalTime(),
                    request.presentationRoom()
            );

            emailService.sendEmail(
                    userEmail,
                    "Actualización de modalidad/agenda - LACSC 2026",
                    message
            );

            return savedSummary;
        }

        if (isAdminSesion()) {
            // Validar que no se intente modificar campos restringidos
            if (request.presentationDateTime() != null && !request.presentationDateTime().equals(summary.getPresentationDateTime())) {
                throw new AccessDeniedException("No tiene permisos para modificar la fecha/hora de la presentación");
            }
            if (request.presentationRoom() != 0 && request.presentationRoom() != summary.getPresentationRoom()) {
                throw new AccessDeniedException("No tiene permisos para modificar la sala de la presentación");
            }
            if (request.authors() != null && !request.authors().isEmpty()) {
                throw new AccessDeniedException("No tiene permisos para modificar los autores");
            }

            summary.setPresentationModality(request.presentationModality());
            savedSummary = summaryRepository.save(summary);
            message = "Hola, tu resumen \"%s\" ha sido programado/actualizado.\nModalidad: %s".formatted(title, request.presentationModality());
            emailService.sendEmail(
                    userEmail,
                    "Actualización de modalidad/agenda - LACSC 2026",
                    message
            );
            return savedSummary;
        }

        throw new AccessDeniedException("No tiene permisos para realizar esta acción");
    }

    @Override
    public Summary reviewSummary(Long id, SummaryReviewDTO review) {
        Summary summary = getById(id);
        log.info("[DEBUG_LOG] Reviewing summary id: {}. Review status: {}. Review Type: {}. Is Admin General: {}. Is Admin Session: {}", 
                id, review.status(), review.type(), isAdminGeneral(), isAdminSesion());

        if (isAdminGeneral() && (ReviewType.PAYMENT == review.type())) {
            return updateSummary(summary, review, review.type());
        }
        if (review.type() != null) {
            if (review.type() == ReviewType.PAYMENT && !isAdminGeneral()) {
                throw new AccessDeniedException("Solo el administrador general puede revisar pagos");
            }

            //?Si es necesario podemos ajustar que el admin general pueda aprovar resumenes ademas de pagos
            //quitar isAdminGeneral()
            if (review.type() == ReviewType.ACADEMIC && (!isAdminSesion() || isAdminGeneral())) {
                throw new AccessDeniedException("No tiene permisos para revisión académica");
            }
            if (review.type() == ReviewType.ACADEMIC && isAdminSesion() && !isAdminGeneral()) {
                List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();
                if (!allowedSessions.contains(summary.getSpecialSession())) {
                    throw new AccessDeniedException("No autorizado para revisar esta sesión");
                }
            }
            
            return updateSummary(summary, review, review.type());
        }
        throw new AccessDeniedException("No autorizado");
    }

    @Override
    public List<Summary> getAllByUserId(Long userId) {
        if (isAdminGeneral() || isAdminPagos() || isAdminRevision() || authService.getCurrentUser().getId().equals(userId)) {
            return summaryRepository.getAllByPresenter_Id(userId).orElseGet(List::of);
        }

        if (isAdminSesion()) {
            List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();
            if (allowedSessions.isEmpty()) {
                return List.of();
            }
            return summaryRepository.findAllByPresenter_IdAndSpecialSessionIn(userId, allowedSessions);
        }

        throw new AccessDeniedException("No tienes permiso para ver estos resúmenes");
    }

    private boolean hasAdminRole() {
        return isAdminGeneral() || isAdminSesion();
    }

    public SummaryCounterDTO getCountOfSummariesByUserId(Long userId) {
        int approved;
        int total;

        log.info("[DEBUG_LOG] Calculating summary count for userId: {}", userId);

        summaryRepository.getAllByPresenter_Id(userId).ifPresent(summaries -> summaries.forEach(s -> log.info("[DEBUG_LOG] Summary ID: {}, Status: {}, Payment: {}, Session: {}",
            s.getId(), s.getSummaryStatus(), s.getSummaryPayment(), s.getSpecialSession())));

        if (isAdminSesion()) {
            List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();
            log.info("[DEBUG_LOG] Admin session roles detected. Allowed sessions: {}", allowedSessions);
            if (allowedSessions.isEmpty()) {
                log.info("[DEBUG_LOG] No session roles found (S_01 to S_16). Falling back to other logic if applicable.");
                if (isAdminGeneral()) {
                    log.info("[DEBUG_LOG] Admin general detected as fallback. Counting by SummaryPayment.");
                    approved = summaryRepository.countAllByPresenter_IdAndSummaryPayment(userId, Status.APPROVED);
                    total = summaryRepository.countAllByPresenter_Id(userId);
                } else {
                    approved = 0;
                    total = 0;
                }
            } else {
                approved = summaryRepository.countAllByPresenter_IdAndSummaryStatusAndSpecialSessionIn(userId, Status.APPROVED, allowedSessions);
                total = summaryRepository.countAllByPresenter_IdAndSpecialSessionIn(userId, allowedSessions);
                log.info("[DEBUG_LOG] Session count result - Approved: {}, Total: {}", approved, total);
            }
        } else if (isAdminGeneral()) {
            log.info("[DEBUG_LOG] Admin general detected. Counting by SummaryPayment.");
            approved = summaryRepository.countAllByPresenter_IdAndSummaryPayment(userId, Status.APPROVED);
            total = summaryRepository.countAllByPresenter_Id(userId);
        } else {
            log.info("[DEBUG_LOG] Owner or other detected. Counting by SummaryStatus.");
            approved = summaryRepository.countAllByPresenter_IdAndSummaryStatus(userId, Status.APPROVED);
            total = summaryRepository.countAllByPresenter_Id(userId);
        }

        log.info("[DEBUG_LOG] Count result - Approved: {}, Total: {}", approved, total);

        return SummaryCounterDTO.builder()
                .approvedSummaries(approved)
                .totalSummaries(total)
                .build();
    }

    private Summary updateSummary(
            Summary summary,
            SummaryReviewDTO review,
            ReviewType type
    ) {
        log.info("[DEBUG_LOG] updateSummary - Type: {}. Status: {}", type, review.status());
        if (type == ReviewType.PAYMENT) {
            summary.setSummaryPayment(review.status());
        } else {
            summary.setSummaryStatus(review.status());
            log.info("[DEBUG_LOG] Updated summaryStatus to: {}", summary.getSummaryStatus());
        }

        Summary saved = summaryRepository.save(summary);
        log.info("[DEBUG_LOG] Saved summary id: {}. summaryStatus: {}", saved.getId(), saved.getSummaryStatus());

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
                  Le informamos que su resumen "%s" ha sido aprobado por el comite academico.
                  """.formatted(title)
                    : """
                  Le informamos que su resumen "%s" ha sido rechazado por el comite academico.
                  Motivo: %s
                  """.formatted(title, review.message());
        }

        emailService.sendEmail(userEmail, subject, message);
    }

}
