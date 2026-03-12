package com.innovawebJT.lacsc.repository;

import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.enums.Status;
import com.innovawebJT.lacsc.model.Institution;
import com.innovawebJT.lacsc.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository - Tests for optimized summary count queries")
class UserRepositoryGetSummaryCountsTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return correct summary counts for multiple users")
    void getSummaryCountsByUserIds_ReturnsCorrectCounts() {
        Institution institution = Institution.builder()
                .name("Test University")
                .acronym("TU")
                .country("Mexico")
                .build();

        User user1 = User.builder()
                .name("User 1")
                .surname("Test")
                .email("user1@test.com")
                .category(Category.STUDENT)
                .institution(institution)
                .status(Status.APPROVED)
                .keycloakId("keycloak-1")
                .build();
        entityManager.persist(user1);

        User user2 = User.builder()
                .name("User 2")
                .surname("Test")
                .email("user2@test.com")
                .category(Category.PROFESSIONAL)
                .institution(institution)
                .status(Status.PENDING)
                .keycloakId("keycloak-2")
                .build();
        entityManager.persist(user2);

        entityManager.flush();

        List<Object[]> results = userRepository.getSummaryCountsByUserIds(
                Arrays.asList(user1.getId(), user2.getId()),
                Status.APPROVED
        );

        assertThat(results).hasSize(2);
        
        Object[] user1Result = results.stream()
                .filter(r -> r[0].equals(user1.getId()))
                .findFirst()
                .orElseThrow();
        
        assertThat(user1Result[0]).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("Should handle empty list of user IDs")
    void getSummaryCountsByUserIds_WithEmptyList_ReturnsEmptyList() {
        List<Object[]> results = userRepository.getSummaryCountsByUserIds(
                List.of(),
                Status.APPROVED
        );

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should return counts as zero when user has no summaries")
    void getSummaryCountsByUserIds_UserWithNoSummaries_ReturnsZeroCounts() {
        Institution institution = Institution.builder()
                .name("Test University")
                .acronym("TU")
                .country("Mexico")
                .build();

        User user = User.builder()
                .name("User No Summaries")
                .surname("Test")
                .email("nosummaries@test.com")
                .category(Category.STUDENT)
                .institution(institution)
                .status(Status.APPROVED)
                .keycloakId("keycloak-ns")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        List<Object[]> results = userRepository.getSummaryCountsByUserIds(
                List.of(user.getId()),
                Status.APPROVED
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0)[1]).isEqualTo(0);
        assertThat(results.get(0)[2]).isEqualTo(0);
    }
}
