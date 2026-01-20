package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.SummaryUpdateRequestDTO;
import com.innovawebJT.lacsc.enums.FileCategory;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Author;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.SummaryRepository;
import com.innovawebJT.lacsc.service.ISummaryService;
import com.innovawebJT.lacsc.service.imp.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SummaryService implements ISummaryService {

    private final SummaryRepository summaryRepository;
    private final FileStorageService fileStorageService;
    private final AuthService authService; // obtiene usuario desde JWT

    @Override
    public Summary create(Summary summary, MultipartFile paymentFile) {

        User currentUser = authService.getCurrentUser();

        String filePath = fileStorageService.store(
    currentUser.getId(),
    FileCategory.SUMMARY_PAYMENT,
    summary.getId(),
    paymentFile
);


        summary.setPresenter(currentUser);
        summary.setReferencePaymentFile(filePath);
        summary.setSummaryPayment(Status.PENDING);

        return summaryRepository.save(summary);
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
    public void delete(Long id) {

        Summary summary = getById(id);
        User currentUser = authService.getCurrentUser();

        if (!summary.getPresenter().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No puedes eliminar este resumen");
        }

        fileStorageService.delete(summary.getReferencePaymentFile());
        summaryRepository.delete(summary);
    }

    @Override
public Summary updateInfo(Long id, SummaryUpdateRequestDTO request) {

    Summary summary = getById(id);
    User currentUser = authService.getCurrentUser();

    if (!summary.getPresenter().getId().equals(currentUser.getId())) {
        throw new AccessDeniedException("No puedes modificar este resumen");
    }

    if (summary.getSummaryPayment() == Status.APPROVED) {
        throw new IllegalStateException(
            "No se puede modificar un resumen aprobado"
        );
    }

    summary.setTitle(request.title());
    summary.setAbstractDescription(request.abstractDescription());
    summary.setSpecialSession(request.specialSession());
    summary.setPresentationModality(request.presentationModality());
    summary.setPresentationDate(request.presentationDate());
    summary.setPresentationRoom(request.presentationRoom());

    // 🔹 autores (reemplazo limpio)
    summary.getAuthors().clear();
    request.authors().forEach(a -> {
        Author author = new Author();
        author.setName(a.name());
        author.setInstitutionName(a.institutionName());
        author.setEmail(a.email());
        author.setSummary(summary);
        summary.getAuthors().add(author);
    });

    return summaryRepository.save(summary);
}

}
