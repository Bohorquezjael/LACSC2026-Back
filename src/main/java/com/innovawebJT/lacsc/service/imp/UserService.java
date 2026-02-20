    package com.innovawebJT.lacsc.service.imp;

    import com.innovawebJT.lacsc.dto.EmergencyContactDTO;
    import com.innovawebJT.lacsc.dto.UserProfileDTO;
    import com.innovawebJT.lacsc.dto.UserResponseDTO;
    import com.innovawebJT.lacsc.enums.FileCategory;
    import com.innovawebJT.lacsc.enums.Status;
    import com.innovawebJT.lacsc.exception.DuplicateUserFieldException;
    import com.innovawebJT.lacsc.exception.UserNotFoundException;
    import com.innovawebJT.lacsc.model.Course;
    import com.innovawebJT.lacsc.model.CourseEnrollment;
    import com.innovawebJT.lacsc.model.EmergencyContact;
    import com.innovawebJT.lacsc.model.User;
    import com.innovawebJT.lacsc.repository.CourseEnrollmentRepository;
    import com.innovawebJT.lacsc.repository.CourseRepository;
    import com.innovawebJT.lacsc.repository.SummaryRepository;
    import com.innovawebJT.lacsc.model.Summary;
    import com.innovawebJT.lacsc.repository.UserRepository;
    import com.innovawebJT.lacsc.security.SecurityUtils;
    import com.innovawebJT.lacsc.service.IUserService;
    import lombok.AllArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.core.io.Resource;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.security.access.AccessDeniedException;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.util.HashSet;
    import java.util.List;

    import com.innovawebJT.lacsc.enums.SpecialSessions;


    @Slf4j
    @Service
    @AllArgsConstructor
    public class UserService implements IUserService {

        private final UserRepository repository;
        private final FileStorageService fileStorageService;
        private final MailSenderNotifications emailService;
        private final CourseRepository courseRepository;
        private final CourseEnrollmentRepository courseEnrollmentRepository;
        private final SummaryService summaryService;
        private final SummaryRepository summaryRepository;

        private boolean hasAdminRole() {
            return SecurityUtils.isAdminGeneral() || SecurityUtils.isAdminSesion();
        }

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

        @Override
        public void validateRegistration(String email, String badgeName) {
            if (email != null && repository.findByEmail(email).isPresent()) {
                throw new DuplicateUserFieldException("email", email);
            }
            if (badgeName != null && !badgeName.isBlank()
                    && repository.findByBadgeName(badgeName).isPresent()) {
                throw new DuplicateUserFieldException("badgeName", badgeName);
            }
        }

        public void reviewUserRegistration(Long userId, Status newStatus, String reason) {
            User user = repository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            user.setStatus(newStatus);
            repository.save(user);
            String message;
            if(Status.APPROVED.equals(newStatus)){
                message = "Te informamos que tu registro ha sido aprobado. Gracias por participar en LACSC 2026.";
            }else{
                message = "Te informamos que tu registro ha sido rechazado. Motivo: " + reason;
            }
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
            if (SecurityUtils.isAdminGeneral()) {
                return repository.findAll(pageable).map(this::mapToUserResponseDTO);
            }

            if (SecurityUtils.isAdminSesion()) {
                List<SpecialSessions> sessions = SecurityUtils.getAllowedSessionsFromRoles();
                if (!sessions.isEmpty()) {
                    return repository.findUsersBySpecialSessions(sessions, pageable)
                            .map(this::mapToUserResponseDTO);
                }
            }
            return Page.empty(pageable);
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

        return mapToResponseDTO(user);
    }

    @Override
    public UserProfileDTO getById(Long id) {
        User u = repository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!SecurityUtils.isAdminGeneral() && !SecurityUtils.isAdminSesion()) {
            throw new AccessDeniedException("No tienes permiso para ver este usuario");
        }

        if (SecurityUtils.isAdminSesion()) {
            List<Summary> userSummariesInAllowedSessions = summaryRepository.findAllByPresenter_IdAndSpecialSessionIn(u.getId(), SecurityUtils.getAllowedSessionsFromRoles());

            if (userSummariesInAllowedSessions.isEmpty()) {
                throw new AccessDeniedException("No tienes permiso para ver este usuario");
            }
        }
        return mapToResponseDTO(u);
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
            boolean isAdmin = hasAdminRole();
            boolean isOwner = user.getKeycloakId().equals(currentKeycloakId);

            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("No tienes permiso para ver este archivo");
            }

            String path = "payment".equals(type) ? user.getReferencePaymentFile() : user.getReferenceStudentFile();

            if (path == null) {
                throw new RuntimeException("El archivo solicitado no existe");
            }

            return fileStorageService.load(path);
        }

        @Override
        public Resource getMyCongressFile(String type) {
            String keycloakId = SecurityUtils.getKeycloakId();
            User user = repository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            String path = "payment".equals(type) ? user.getReferencePaymentFile() : user.getReferenceStudentFile();

            if (path == null) {
                throw new RuntimeException("El archivo solicitado no existe");
            }

            return fileStorageService.load(path);
        }

        @Override
        public void enrollCurrentUserToCourse(Long courseId, MultipartFile paymentFile) {
            String keycloakId = SecurityUtils.getKeycloakId();
            User user = repository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new UserNotFoundException("Course not found"));

            CourseEnrollment enrollment = courseEnrollmentRepository
                    .findByUserIdAndCourseId(user.getId(), courseId)
                    .orElseGet(() -> CourseEnrollment.builder()
                            .user(user)
                            .course(course)
                            .paymentStatus(Status.PENDING)
                            .build());

            String paymentPath = fileStorageService.store(
                    user.getId(),
                    FileCategory.COURSE_PAYMENT,
                    "course_" + courseId,
                    paymentFile
            );
            enrollment.setReferencePaymentFile(paymentPath);
            enrollment.setPaymentStatus(Status.PENDING);

            if (user.getCourses() == null) {
                user.setCourses(new HashSet<>());
            }
            user.getCourses().add(course);

            courseEnrollmentRepository.save(enrollment);
            repository.save(user);
        }

        @Override
        public Resource getMyCoursePaymentFile(Long courseId) {
            String keycloakId = SecurityUtils.getKeycloakId();
            User user = repository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            CourseEnrollment enrollment = courseEnrollmentRepository
                    .findByUserIdAndCourseId(user.getId(), courseId)
                    .orElseThrow(() -> new UserNotFoundException("Enrollment not found"));

            String path = enrollment.getReferencePaymentFile();
            if (path == null) {
                throw new RuntimeException("El archivo solicitado no existe");
            }
            return fileStorageService.load(path);
        }

        @Override
        public Resource getCoursePaymentFile(Long userId, Long courseId) {
            User targetUser = repository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            boolean isOwner = targetUser.getKeycloakId().equals(SecurityUtils.getKeycloakId());
            boolean isAdmin = SecurityUtils.isAdminGeneral();

            if (!isOwner && !isAdmin) {
                throw new AccessDeniedException("No tienes permiso para ver este archivo");
            }

            CourseEnrollment enrollment = courseEnrollmentRepository
                    .findByUserIdAndCourseId(targetUser.getId(), courseId)
                    .orElseThrow(() -> new UserNotFoundException("Enrollment not found"));

            String path = enrollment.getReferencePaymentFile();
            if (path == null) {
                throw new RuntimeException("El archivo solicitado no existe");
            }
            return fileStorageService.load(path);
        }

        @Override
        public void reviewCoursePayment(Long userId, Long courseId, Status status, String message) {
            if (!SecurityUtils.isAdminGeneral()) {
                throw new AccessDeniedException("Solo el administrador general puede revisar pagos");
            }

            CourseEnrollment enrollment = courseEnrollmentRepository
                    .findByUserIdAndCourseId(userId, courseId)
                    .orElseThrow(() -> new UserNotFoundException("Inscripción al curso no encontrada"));

            enrollment.setPaymentStatus(status);
            courseEnrollmentRepository.save(enrollment);

            String emailBody;
            if (Status.APPROVED.equals(status)) {
                emailBody = "Tu pago para el curso '%s' ha sido aprobado.".formatted(enrollment.getCourse().getName());
            } else {
                emailBody = "Tu pago para el curso '%s' ha sido rechazado. Motivo: %s".formatted(enrollment.getCourse().getName(), message);
            }

            emailService.sendEmail(enrollment.getUser().getEmail(),
                    "Actualización de Pago de Curso - LACSC 2026",
                    emailBody);
        }

        @Override
        public void reuploadCongressPayment(MultipartFile paymentFile, MultipartFile studentFile) {
            String keycloakId = SecurityUtils.getKeycloakId();
            User user = repository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (user.getStatus() != Status.REJECTED) {
                throw new IllegalStateException("Solo puedes reenviar comprobantes si tu registro fue rechazado");
            }

            if (paymentFile != null && !paymentFile.isEmpty()) {
                String newPath = fileStorageService.replace(user.getReferencePaymentFile(), paymentFile);
                user.setReferencePaymentFile(newPath);
            }

            if (studentFile != null && !studentFile.isEmpty()) {
                if (user.getReferenceStudentFile() != null) {
                    String newPath = fileStorageService.replace(user.getReferenceStudentFile(), studentFile);
                    user.setReferenceStudentFile(newPath);
                } else {
                    String newPath = fileStorageService.store(
                            user.getId(),
                            FileCategory.STUDENT_VERIFICATION,
                            "student",
                            studentFile
                    );
                    user.setReferenceStudentFile(newPath);
                }
            }

            user.setStatus(Status.PENDING);
            repository.save(user);

            emailService.sendEmail(user.getEmail(), "Reenvío de comprobantes - LACSC 2026",
                    "Hemos recibido tus nuevos comprobantes para el congreso. En breve un administrador los revisará.");
        }

        @Override
        public void reuploadCoursePayment(Long courseId, MultipartFile paymentFile) {
            String keycloakId = SecurityUtils.getKeycloakId();
            User user = repository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            CourseEnrollment enrollment = courseEnrollmentRepository
                    .findByUserIdAndCourseId(user.getId(), courseId)
                    .orElseThrow(() -> new UserNotFoundException("Inscripción al curso no encontrada"));

            if (enrollment.getPaymentStatus() != Status.REJECTED) {
                throw new IllegalStateException("Solo puedes reenviar comprobantes si el pago fue rechazado");
            }

            String newPath = fileStorageService.replace(enrollment.getReferencePaymentFile(), paymentFile);
            enrollment.setReferencePaymentFile(newPath);
            enrollment.setPaymentStatus(Status.PENDING);

            courseEnrollmentRepository.save(enrollment);

            emailService.sendEmail(user.getEmail(), "Reenvío de pago de curso - LACSC 2026",
                    "Hemos recibido tu nuevo comprobante para el curso '%s'. En breve un administrador lo revisará.".formatted(enrollment.getCourse().getName()));
        }

        @Override
        public Page<UserResponseDTO> scholarshipCandidates(Pageable pageable) {

            return null;
        }

        @Override
        public List<Course> getMyCourses() {
            String keycloakId = SecurityUtils.getKeycloakId();
            User user = repository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            return List.copyOf(user.getCourses());
        }

        private UserProfileDTO mapToResponseDTO(User user) {
            return UserProfileDTO.builder()
                    .name(user.getName())
                    .surname(user.getSurname())
                    .cellphone(user.getCellphone())
                    .gender(user.getGender())
                    .country(user.getCountry())
                    .badgeName(user.getBadgeName())
                    .email(user.getEmail())
                    .category(user.getCategory())
                    .institution(user.getInstitution())
                    .emergencyContact(mapToResponseContactDTO(user.getEmergencyContact()))
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .referencePaymentFile(user.getReferencePaymentFile())
                    .referenceStudentFile(user.getReferenceStudentFile())
                    .build();
        }

        private UserResponseDTO mapToUserResponseDTO(User user) {
            return UserResponseDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .surname(user.getSurname())
                    .email(user.getEmail())
                    .status(user.getStatus())
                    .institution(user.getInstitution())
                    .category(user.getCategory())
                    .summariesReviewed(summaryService.getCountOfSummariesByUserId(user.getId()))
                    .build();
        }

        private EmergencyContactDTO mapToResponseContactDTO(EmergencyContact contact) {
            return EmergencyContactDTO.builder()
                    .fullName(contact.getName())
                    .relationship(contact.getRelationship())
                    .phone(contact.getCellphone())
                    .build();
        }


    }
