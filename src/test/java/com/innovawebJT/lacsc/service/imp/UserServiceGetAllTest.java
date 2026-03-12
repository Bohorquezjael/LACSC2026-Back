package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.enums.SpecialSessions;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.SummaryRepository;
import com.innovawebJT.lacsc.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Tests for getAll() method optimization")
class UserServiceGetAllTest {

    @Mock
    private UserRepository repository;

    @Mock
    private SummaryRepository summaryRepository;

    @Mock
    private com.innovawebJT.lacsc.service.imp.FileStorageService fileStorageService;

    @Mock
    private com.innovawebJT.lacsc.service.imp.MailSenderNotifications emailService;

    @Mock
    private com.innovawebJT.lacsc.repository.CourseRepository courseRepository;

    @Mock
    private com.innovawebJT.lacsc.repository.CourseEnrollmentRepository courseEnrollmentRepository;

    @Mock
    private SummaryService summaryService;

    @InjectMocks
    private UserService userService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 12);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(String... roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "testuser", null, authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should call repository with optimized query for Admin General")
    void getAll_AsAdminGeneral_CallsOptimizedQuery() {
        setSecurityContext("ROLE_ADMIN_GENERAL");
        
        User user = User.builder()
                .id(1L)
                .name("Test")
                .surname("User")
                .email("test@test.com")
                .status(Status.APPROVED)
                .build();

        Page<User> userPage = new PageImpl<>(Collections.singletonList(user), pageable, 1);

        List<Object[]> summaryCounts = new ArrayList<>();
        summaryCounts.add(new Object[]{1L, 5, 10});

        when(repository.findAll(pageable)).thenReturn(userPage);
        when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                .thenReturn(summaryCounts);

        Page<UserResponseDTO> result = userService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(repository, times(1)).findAll(pageable);
        verify(repository, times(1)).getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED));
    }

    @Test
    @DisplayName("Should return empty page when no users found")
    void getAll_ReturnsEmptyPage() {
        setSecurityContext("ROLE_ADMIN_GENERAL");
        
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);
        when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                .thenReturn(Collections.emptyList());

        Page<UserResponseDTO> result = userService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should handle Admin Revision role correctly")
    void getAll_AsAdminRevision_CallsOptimizedQuery() {
        setSecurityContext("ROLE_ADMIN_REVISION");
        
        User user = User.builder()
                .id(1L)
                .name("Test")
                .surname("User")
                .email("test@test.com")
                .status(Status.APPROVED)
                .build();

        Page<User> userPage = new PageImpl<>(Collections.singletonList(user), pageable, 1);

        List<Object[]> summaryCounts = new ArrayList<>();
        summaryCounts.add(new Object[]{1L, 2, 4});

        when(repository.findAll(pageable)).thenReturn(userPage);
        when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                .thenReturn(summaryCounts);

        Page<UserResponseDTO> result = userService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(repository).findAll(pageable);
    }
}
