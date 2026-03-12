package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.dto.SummaryCounterDTO;
import com.innovawebJT.lacsc.dto.UserResponseDTO;
import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.enums.SpecialSessions;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Institution;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.SummaryRepository;
import com.innovawebJT.lacsc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private FileStorageService fileStorageService;

    @Mock
    private MailSenderNotifications emailService;

    @Mock
    private com.innovawebJT.lacsc.repository.CourseRepository courseRepository;

    @Mock
    private com.innovawebJT.lacsc.repository.CourseEnrollmentRepository courseEnrollmentRepository;

    @Mock
    private SummaryService summaryService;

    @InjectMocks
    private UserService userService;

    private Pageable pageable;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 12);

        Institution institution1 = Institution.builder()
                .institutionName("Test University")
                .institutionAcronym("TU")
                .institutionCountry("Mexico")
                .build();

        testUser1 = User.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .email("john.doe@test.com")
                .category(Category.STUDENT_UNDERGRADUATE)
                .institution(institution1)
                .status(Status.APPROVED)
                .keycloakId("keycloak-1")
                .build();

        Institution institution2 = Institution.builder()
                .institutionName("Another University")
                .institutionAcronym("AU")
                .institutionCountry("USA")
                .build();

        testUser2 = User.builder()
                .id(2L)
                .name("Jane")
                .surname("Smith")
                .email("jane.smith@test.com")
                .category(Category.PROFESSIONAL)
                .institution(institution2)
                .status(Status.PENDING)
                .keycloakId("keycloak-2")
                .build();
    }

    @Nested
    @DisplayName("Admin General - Optimized Query Tests")
    class AdminGeneralTests {

        @Test
        @DisplayName("Should return paginated users with summary counts for Admin General")
        void getAll_AsAdminGeneral_ReturnsUsersWithSummaryCounts() {
            List<User> users = Arrays.asList(testUser1, testUser2);
            Page<User> userPage = new PageImpl<>(users, pageable, 2);

            List<Object[]> summaryCounts = Arrays.asList(
                    new Object[]{1L, 5, 10},
                    new Object[]{2L, 3, 7}
            );

            when(repository.findAll(pageable)).thenReturn(userPage);
            when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                    .thenReturn(summaryCounts);

            Page<UserResponseDTO> result = userService.getAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).name()).isEqualTo("John");
            assertThat(result.getContent().get(0).summariesReviewed().approvedSummaries()).isEqualTo(5);
            assertThat(result.getContent().get(0).summariesReviewed().totalSummaries()).isEqualTo(10);
            assertThat(result.getContent().get(1).name()).isEqualTo("Jane");
            assertThat(result.getContent().get(1).summariesReviewed().approvedSummaries()).isEqualTo(3);
            assertThat(result.getContent().get(1).summariesReviewed().totalSummaries()).isEqualTo(7);

            verify(repository, times(1)).findAll(pageable);
            verify(repository, times(1)).getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED));
        }

        @Test
        @DisplayName("Should handle empty page correctly")
        void getAll_AsAdminGeneral_ReturnsEmptyPage() {
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repository.findAll(pageable)).thenReturn(emptyPage);
            when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                    .thenReturn(Collections.emptyList());

            Page<UserResponseDTO> result = userService.getAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should use default counts when no summary counts found for user")
        void getAll_AsAdminGeneral_HandlesMissingSummaryCounts() {
            List<User> users = List.of(testUser1);
            Page<User> userPage = new PageImpl<>(users, pageable, 1);

            when(repository.findAll(pageable)).thenReturn(userPage);
            when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                    .thenReturn(Collections.emptyList());

            Page<UserResponseDTO> result = userService.getAll(pageable);

            assertThat(result.getContent().get(0).summariesReviewed().approvedSummaries()).isEqualTo(0);
            assertThat(result.getContent().get(0).summariesReviewed().totalSummaries()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Admin Sesion - Query with Special Sessions Tests")
    class AdminSesionTests {

        @Test
        @DisplayName("Should return users filtered by special sessions")
        void getAll_AsAdminSesion_ReturnsFilteredUsers() {
            List<SpecialSessions> allowedSessions = List.of(SpecialSessions.S_03);
            List<User> users = List.of(testUser1);
            Page<User> userPage = new PageImpl<>(users, pageable, 1);

            List<Object[]> summaryCounts = List.<Object[]>of(
                    new Object[]{1L, 2L, 4L}
            );

            when(repository.findUsersBySpecialSessions(allowedSessions, pageable))
                    .thenReturn(userPage);
            when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                    .thenReturn(summaryCounts);

            Page<UserResponseDTO> result = userService.getAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("John");

            verify(repository).findUsersBySpecialSessions(allowedSessions, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no special sessions allowed")
        void getAll_AsAdminSesion_WithEmptySessions_ReturnsEmptyPage() {
            when(repository.findUsersBySpecialSessions(any(), any(Pageable.class)))
                    .thenReturn(Page.empty(pageable));

            Page<UserResponseDTO> result = userService.getAll(pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Query Optimization Verification Tests")
    class QueryOptimizationTests {

        @Test
        @DisplayName("Should execute exactly 2 queries instead of N+1 (one for users, one for counts)")
        void getAll_VerifiesQueryCount_IsOptimized() {
            List<User> users = Arrays.asList(testUser1, testUser2);
            Page<User> userPage = new PageImpl<>(users, pageable, 2);

            List<Object[]> summaryCounts = Arrays.asList(
                    new Object[]{1L, 5, 10},
                    new Object[]{2L, 3, 7}
            );

            when(repository.findAll(pageable)).thenReturn(userPage);
            when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                    .thenReturn(summaryCounts);

            userService.getAll(pageable);

            verify(repository, times(1)).findAll(pageable);
            verify(repository, times(1)).getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED));
            verify(summaryService, times(0)).getCountOfSummariesByUserId(any());
        }

        @Test
        @DisplayName("Should batch summary counts request for all users in page")
        void getAll_BatchesSummaryCountRequests() {
            int pageSize = 12;
            Pageable largePage = PageRequest.of(0, pageSize);
            
            List<User> users = new java.util.ArrayList<>();
            for (long i = 1; i <= pageSize; i++) {
                users.add(User.builder()
                        .id(i)
                        .name("User " + i)
                        .surname("Test")
                        .email("user" + i + "@test.com")
                        .status(Status.APPROVED)
                        .keycloakId("keycloak-" + i)
                        .build());
            }
            
            Page<User> userPage = new PageImpl<>(users, largePage, pageSize);

            when(repository.findAll(largePage)).thenReturn(userPage);
            when(repository.getSummaryCountsByUserIds(anyList(), eq(Status.APPROVED)))
                    .thenReturn(Collections.emptyList());

            userService.getAll(largePage);

            verify(repository).getSummaryCountsByUserIds(
                    org.mockito.ArgumentMatchers.argThat(list -> list.size() == pageSize),
                    eq(Status.APPROVED)
            );
        }
    }
}
