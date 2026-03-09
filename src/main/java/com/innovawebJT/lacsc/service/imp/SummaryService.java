package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.audit.Audit;
import com.innovawebJT.lacsc.dto.*;
import com.innovawebJT.lacsc.enums.*;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.SummaryRepository;
import com.innovawebJT.lacsc.service.ISummaryService;
import com.innovawebJT.lacsc.util.Helpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static com.innovawebJT.lacsc.security.SecurityUtils.*;
import static com.innovawebJT.lacsc.util.Helpers.mapToSummaryDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryService implements ISummaryService {

    private final SummaryRepository summaryRepository;
    private final FileStorageService fileStorageService;
    private final AuthService authService;
    private final MailSenderNotifications emailService;

    @Audit(action = "CREATE_SUMMARY", entity = "SUMMARY")
    @Override
    public SummaryDTO create(Summary summary, MultipartFile paymentFile) {

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
        return mapToSummaryDTO(savedSummary);
    }

    @Audit(action = "REUPLOAD_SUMMARY_PAYMENT", entity = "SUMMARY")
    @Override
    public void reuploadPaymentProof(Long summaryId, MultipartFile file) {

        Summary summary = summaryRepository.findById(summaryId).orElseThrow(() -> new RuntimeException("Resumen no encontrado"));
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
    public Page<SummaryDTO> getAll(Pageable pageable) {

        if (isAdminGeneral() || isAdminPagos() || isAdminRevision()) {
            log.info("Admin general");
            return summaryRepository.findAll(pageable).map(Helpers::mapToSummaryDTO);
        }

        if (isAdminSesion()) {
            log.info("Admin sesion");
            List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();

            if (allowedSessions.isEmpty()) {
                return Page.empty(pageable);
            }

            return summaryRepository
                    .findBySpecialSessionIn(allowedSessions, pageable).map(Helpers::mapToSummaryDTO);
        }
        log.error("No entra a rol");
        throw new AccessDeniedException("No autorizado");
    }

    @Override
    public Page<SummaryDTO> getMine(Pageable pageable) {
        Long userId = authService.getCurrentUser().getId();
        return summaryRepository.getAllByPresenter_Id(userId, pageable).map(Helpers::mapToSummaryDTO);
    }

    @Override
    public SummaryDTO getById(Long id) {
        Summary summary = summaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Resumen no encontrado"));
        if(!isAdminGeneral() && !isAdminRevision() && !isAdminPagos() && !summary.getPresenter().getId().equals(authService.getCurrentUser().getId())) {
            throw new AccessDeniedException("No tienes permiso para ver este resumen");
        }
        if(isAdminSesion() && !getAllowedSessionsFromRoles().contains(summary.getSpecialSession())) {
            throw new AccessDeniedException("No estas autorizado para ver resumenes de otras sesiones.");
        }
        return mapToSummaryDTO(summary);
    }

    @Audit(action = "DOWNLOAD_SUMMARY_PAYMENT", entity = "SUMMARY")
    @Override
    public Resource getPaymentResource(Long id) {
        Summary summary = summaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Resumen no encontrado"));
        boolean isAdmin = isAdminGeneral() || isAdminPagos() || isAdminRevision();
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

    @Audit(action = "DELETE_SUMMARY", entity = "SUMMARY")
    @Override
    public void delete(Long id) {
        Summary summary = summaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Resumen no encontrado"));
        boolean isAdmin = hasAdminRole();
        User currentUser = isAdmin ? null : authService.getCurrentUser();

        boolean isOwner = !isAdmin && summary.getPresenter().getId().equals(currentUser.getId());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No puedes eliminar este resumen");
        }

        fileStorageService.delete(summary.getReferencePaymentFile());
        summaryRepository.delete(summary);
    }

    @Audit(action = "UPDATE_SUMMARY_INFO", entity = "SUMMARY")
    @Override
    public SummaryDTO updateInfo(Long id, SummaryUpdateRequestDTO request) {
        Summary summary = summaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Resumen no encontrado"));
        String userEmail = summary.getPresenter().getEmail();
        String title = summary.getTitle();
        String message;
        Summary savedSummary;

        if (isAdminGeneral()) {
            summary.setPresentationModality(request.presentationModality());
            summary.setPresentationDateTime(request.presentationDateTime());
            summary.setPresentationRoom(request.presentationRoom());
            savedSummary = summaryRepository.save(summary);



            //modalidad por aparte y los demas campos juntos
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

            return mapToSummaryDTO(savedSummary);
        }

        if (isAdminSesion()) {
            // Validar que no se intente modificar campos restringidos
            if (request.presentationDateTime() != null && !request.presentationDateTime().equals(summary.getPresentationDateTime())) {
                throw new AccessDeniedException("No tiene permisos para modificar la fecha/hora de la presentación");
            }
            if (!Objects.equals(request.presentationRoom(), "") && !Objects.equals(request.presentationRoom(), summary.getPresentationRoom())) {
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
            return mapToSummaryDTO(savedSummary);
        }

        throw new AccessDeniedException("No tiene permisos para realizar esta acción");
    }

    @Audit(action = "ASSIGN_SCHEDULE", entity = "SUMMARY")
    @Override
    public SummaryDTO updateSchedule(Long id, SummaryScheduleDTO request) {

        Summary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resumen no encontrado"));

        if (summary.getPresentationModality() == PresentationModality.ORAL) {

            boolean exists = summaryRepository
                    .existsByPresentationDateTimeAndPresentationRoomAndPresentationModalityAndIdNot(
                            request.presentationDateTime(),
                            request.presentationRoom(),
                            PresentationModality.ORAL,
                            id
                    );

            if (exists) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La sala ya está ocupada en ese horario"
                );
            }
        }

        summary.setPresentationDateTime(request.presentationDateTime());
        summary.setPresentationRoom(request.presentationRoom());

        Summary saved = summaryRepository.save(summary);

        sendScheduleEmail(summary);

        return mapToSummaryDTO(saved);
    }

    @Audit(action = "UPDATE_MODALITY", entity = "SUMMARY")
    @Override
    public SummaryDTO updateModality(Long id, SummaryModalityDTO request) {

        Summary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resumen no encontrado"));

        summary.setPresentationModality(request.presentationModality());

        if (request.presentationModality() == PresentationModality.POSTER) {
            summary.setPresentationDateTime(null);
            summary.setPresentationRoom(null);
        }

        Summary saved = summaryRepository.save(summary);

        sendModalityEmail(summary);

        return mapToSummaryDTO(saved);
    }

    @Audit(action = "REVIEW_SUMMARY", entity = "SUMMARY")
    @Override
    public SummaryDTO reviewSummary(Long id, SummaryReviewDTO review) {
        Summary summary = summaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Resumen no encontrado"));
        log.info("[DEBUG_LOG] Reviewing summary id: {}. Review status: {}. Review Type: {}. Is Admin General: {}. Is Admin Session: {}. Is Admin Pagos: {}", 
                id, review.status(), review.type(), isAdminGeneral(), isAdminSesion(), isAdminPagos());

        if (review.type() == null) {
            throw new AccessDeniedException("No autorizado");
        }

        if (review.type() == ReviewType.PAYMENT) {
            if (!isAdminGeneral() && !isAdminPagos()) {
                throw new AccessDeniedException("No tienes permiso para revisar pagos");
            }
            return mapToSummaryDTO(updateSummary(summary, review, review.type()));
        }

        if (review.type() == ReviewType.ACADEMIC) {
            if (!isAdminSesion() && !isAdminGeneral()) {
                throw new AccessDeniedException("No tiene permisos para revisión académica");
            }
            if (isAdminSesion() && !isAdminGeneral()) {
                List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();
                if (!allowedSessions.contains(summary.getSpecialSession())) {
                    throw new AccessDeniedException("No autorizado para revisar esta sesión");
                }
            }
            return mapToSummaryDTO(updateSummary(summary, review, review.type()));
        }

        throw new AccessDeniedException("No autorizado");
    }

    @Override
    public Page<SummaryDTO> getAllByUserId(Long userId, Pageable pageable) {
        if (isAdminGeneral() || isAdminPagos() || isAdminRevision() || authService.getCurrentUser().getId().equals(userId)) {
            return summaryRepository.getAllByPresenter_Id(userId, pageable).map(Helpers::mapToSummaryDTO);
        }

        if (isAdminSesion()) {
            List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();
            if (allowedSessions.isEmpty()) {
                return Page.empty();
            }
            return summaryRepository.findAllByPresenter_IdAndSpecialSessionIn(userId, allowedSessions, pageable).map(Helpers::mapToSummaryDTO);
        }

        throw new AccessDeniedException("No tienes permiso para ver estos resúmenes");
    }

    private boolean hasAdminRole() {
        return isAdminGeneral() || isAdminSesion();
    }

    public SummaryCounterDTO getCountOfSummariesByUserId(Long userId) {

        int approved;
        int total;

        if (isAdminPagos()) {
            approved = summaryRepository
                    .countAllByPresenter_IdAndSummaryPayment(userId, Status.APPROVED);
            total = summaryRepository
                    .countAllByPresenter_Id(userId);

        } else if (isAdminRevision()) {
            approved = summaryRepository
                    .countAllByPresenter_IdAndSummaryStatus(userId, Status.APPROVED);
            total = summaryRepository
                    .countAllByPresenter_Id(userId);

        } else if (isAdminSesion()) {

            List<SpecialSessions> allowedSessions = getAllowedSessionsFromRoles();

            if (allowedSessions.isEmpty()) {
                approved = 0;
                total = 0;
            } else {
                approved = summaryRepository
                        .countAllByPresenter_IdAndSummaryStatusAndSpecialSessionIn(
                                userId, Status.APPROVED, allowedSessions);

                total = summaryRepository
                        .countAllByPresenter_IdAndSpecialSessionIn(
                                userId, allowedSessions);
            }

        } else if (isAdminGeneral()) {

            approved = summaryRepository
                    .countAllByPresenter_IdAndSummaryPayment(userId, Status.APPROVED);

            total = summaryRepository
                    .countAllByPresenter_Id(userId);

        } else {

            approved = summaryRepository
                    .countAllByPresenter_IdAndSummaryStatus(userId, Status.APPROVED);

            total = summaryRepository
                    .countAllByPresenter_Id(userId);
        }

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

    private void sendScheduleEmail(Summary summary) {

        String message = """
            Hola, tu resumen "%s" ha sido reprogramado.
            Fecha: %s
            Hora: %s
            Sala: %s
            """.formatted(
                summary.getTitle(),
                summary.getPresentationDateTime().toLocalDate(),
                summary.getPresentationDateTime().toLocalTime(),
                summary.getPresentationRoom()
        );

        emailService.sendEmail(
                summary.getPresenter().getEmail(),
                "Actualización de agenda - LACSC 2026",
                message
        );
    }

    private void sendModalityEmail(Summary summary) {

        String message = """
            Hola, tu resumen "%s" ha cambiado de modalidad.
            Nueva modalidad: %s
            """.formatted(
                summary.getTitle(),
                summary.getPresentationModality()
        );

        emailService.sendEmail(
                summary.getPresenter().getEmail(),
                "Actualización de modalidad - LACSC 2026",
                message
        );
    }
}
