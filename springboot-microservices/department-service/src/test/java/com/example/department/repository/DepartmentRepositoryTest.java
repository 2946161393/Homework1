package com.example.department.repository;

import com.example.department.domain.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Department Repository 测试
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("DepartmentRepository Tests")
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find department by code ignoring case")
    void shouldFindByCodeIgnoreCase() {
        // Given
        Department dept = Department.builder()
                .name("Engineering")
                .code("ENG")
                .description("Engineering Department")
                .build();

        entityManager.persistAndFlush(dept);

        // When
        Optional<Department> result = repository.findByCodeIgnoreCase("eng");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Engineering");
    }

    @Test
    @DisplayName("Should check if code exists")
    void shouldCheckCodeExists() {
        // Given
        Department dept = Department.builder()
                .name("HR")
                .code("HR001")
                .build();

        entityManager.persistAndFlush(dept);

        // When
        boolean exists = repository.existsByCode("HR001");
        boolean notExists = repository.existsByCode("FINANCE");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find departments with filters")
    void shouldFindWithFilters() {
        // Given
        Department dept1 = Department.builder()
                .name("Engineering")
                .code("ENG")
                .build();

        Department dept2 = Department.builder()
                .name("Finance")
                .code("FIN")
                .build();

        entityManager.persist(dept1);
        entityManager.persist(dept2);
        entityManager.flush();

        // When
        Page<Department> result = repository.findWithFilters("Engineering", null, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Engineering");
    }
}