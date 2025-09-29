package com.example.employee.repository;

import com.example.employee.domain.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@DisplayName("EmployeeRepository Tests")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save and find employee by email")
    void shouldSaveAndFindByEmail() {
        // Given
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .departmentId(1L)
                .build();

        // When
        entityManager.persistAndFlush(employee);
        boolean exists = repository.existsByEmail("john.doe@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find employees by department id")
    void shouldFindByDepartmentId() {
        // Given
        Employee emp1 = Employee.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .departmentId(1L)
                .build();

        Employee emp2 = Employee.builder()
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob@example.com")
                .departmentId(1L)
                .build();

        entityManager.persist(emp1);
        entityManager.persist(emp2);
        entityManager.flush();

        // When
        Page<Employee> result = repository.findByDepartmentId(1L, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Employee::getEmail)
                .containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
    }

    @Test
    @DisplayName("Should search employees by name or email")
    void shouldSearchByNameOrEmail() {
        // Given
        Employee employee = Employee.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie.brown@example.com")
                .build();

        entityManager.persistAndFlush(employee);

        // When
        Page<Employee> result = repository.searchByNameOrEmail("Charlie", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Charlie");
    }

    @Test
    @DisplayName("Should check email uniqueness excluding specific id")
    void shouldCheckEmailUniqueness() {
        // Given
        Employee employee = Employee.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();

        Employee saved = entityManager.persistAndFlush(employee);

        // When
        boolean existsForOtherId = repository.existsByEmailAndIdNot("test@example.com", saved.getId() + 1);
        boolean existsForSameId = repository.existsByEmailAndIdNot("test@example.com", saved.getId());

        // Then
        assertThat(existsForOtherId).isTrue();
        assertThat(existsForSameId).isFalse();
    }
}