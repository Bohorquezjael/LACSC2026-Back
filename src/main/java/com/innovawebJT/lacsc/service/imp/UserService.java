package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.EmergencyContactDTO;
import com.innovawebJT.lacsc.dto.UserProfileDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.enums.FileCategory;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.exception.UserNotFoundException;
import com.innovawebJT.lacsc.model.EmergencyContact;
import com.innovawebJT.lacsc.model.Summary;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.UserRepository;
import com.innovawebJT.lacsc.security.SecurityUtils;
import com.innovawebJT.lacsc.service.ISummaryService;
import com.innovawebJT.lacsc.service.IUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService implements IUserService {

    private final UserRepository repository;
    private final ISummaryService summaryRepository;
    private final FileStorageService fileStorageService;
    private final MailSenderNotifications emailService;

    @Override
    public UserResponseDTO createOrUpdateProfile(String keycloakId, UserProfileDTO dto) {

        boolean isNewUser = !repository.existsByKeycloakId(keycloakId);

    User user = repository.findByKeycloakId(keycloakId)
            .orElseGet(() -> {
                User u = new User();
                u.setKeycloakId(keycloakId);
                return u;
            });
    user.setName(dto.name());
    user.setSurname(dto.surname());
    user.setBadgeName(dto.badgeName());
    user.setEmail(dto.email());
    user.setCategory(dto.category());
    user.setInstitution(dto.institution());
    user.setCellphone(dto.cellphone());
    user.setGender(dto.gender());
    user.setCountry(dto.country());
    user.setStatus(dto.status());

    if (dto.emergencyContact() != null) {
        EmergencyContact contact = EmergencyContact.builder()
            .name(dto.emergencyContact().fullName())
            .relationship(dto.emergencyContact().relationship())
            .cellphone(dto.emergencyContact().phone())
            .user(user)
            .build();

        user.setEmergencyContact(contact);
    }

    User saved = repository.save(user);
    if (isNewUser) {
            emailService.sendEmail(saved.getEmail(), "¡Bienvenido a LACSC 2026!",
                "Tu perfil ha sido creado exitosamente. Ya puedes registrar tus resúmenes y subir tus comprobantes de pago.");
        }
    return UserResponseDTO.builder()
            .id(saved.getId())
            .name(saved.getName())
            .surname(saved.getSurname())
            .email(saved.getEmail())
            .status(saved.getStatus())
            .institution(saved.getInstitution())
            .category(saved.getCategory())
            .build();
}

    public void reviewUserRegistration(Long userId, Status newStatus, String message) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setStatus(newStatus);
        repository.save(user);

        emailService.sendEmail(user.getEmail(),
            "Actualización de Inscripción - LACSC 2026",
            message);
    }

    @Override
public UserResponseDTO getProfile(String keycloakId) {
    User user = repository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new UserNotFoundException("Profile not found"));

    return UserResponseDTO.builder()
            .id(user.getId())
            .category(user.getCategory())
            .build();
}


    @Override
    public Page<UserResponseDTO> getAll(Pageable pageable) {
        return repository.findAllUsers(pageable);
    }

    @Override
    public boolean deleteUser(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
public UserProfileDTO getCurrentUser() {

    String keycloakId = SecurityUtils.getKeycloakId();
    log.info("User logged in: {}", keycloakId);
    User user = repository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new UserNotFoundException("Profile not found"));

    return UserProfileDTO.builder()
            .name(user.getName())
            .surname(user.getSurname())
            .badgeName(user.getBadgeName())
            .cellphone(user.getCellphone())
            .gender(user.getGender())
            .email(user.getEmail())
            .country(user.getCountry())
            .category(user.getCategory())
            .institution(user.getInstitution())
            .emergencyContact(
                    EmergencyContactDTO.builder()
                            .fullName(user.getEmergencyContact().getName())
                            .relationship(user.getEmergencyContact().getRelationship())
                            .phone(user.getEmergencyContact().getCellphone())
                            .build()
            )
            .status(user.getStatus())
            .build();
}

    /**
     * @param id
     * @return
     */
    @Override
    public UserResponseDTO getById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public void enrollToCongress(MultipartFile paymentFile, MultipartFile studentFile) {
        String keycloakId = SecurityUtils.getKeycloakId();
        User user = repository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Guardar archivo de pago
        String paymentPath = fileStorageService.store(
            user.getId(),
            FileCategory.CONGRESS_PAYMENT,
            "congress",
            paymentFile
        );
        user.setReferencePaymentFile(paymentPath);

        // Guardar comprobante de estudiante si viene
        if (studentFile != null && !studentFile.isEmpty()) {
            String studentPath = fileStorageService.store(
                user.getId(),
                FileCategory.STUDENT_VERIFICATION,
                "student",
                studentFile
            );
            user.setReferenceStudentFile(studentPath);
        }

        user.setStatus(Status.PENDING);
        repository.save(user);

        emailService.sendEmail(user.getEmail(), "Inscripción al Congreso en Revisión",
            "Hemos recibido tus comprobantes. En breve un administrador revisará tu pago.");
    }

    public Resource getCongressFile(Long userId, String type) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String currentKeycloakId = SecurityUtils.getKeycloakId();
        boolean isOwner = user.getKeycloakId().equals(currentKeycloakId);
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para ver este archivo");
        }

        String path = "payment".equals(type) ? user.getReferencePaymentFile() : user.getReferenceStudentFile();

        if (path == null) {
            throw new RuntimeException("El archivo solicitado no existe");
        }

        return fileStorageService.load(path);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .category(user.getCategory())
                .institution(user.getInstitution())
                .status(user.getStatus())
                .build();
    }
}
