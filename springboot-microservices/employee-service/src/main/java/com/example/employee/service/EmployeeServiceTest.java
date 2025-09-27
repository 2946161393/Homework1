package com.example.employee.service;

import com.example.employee.client.DepartmentClient;
import com.example.employee.domain.Employee;
import com.example.employee.dto.DepartmentDTO;
import com.example.employee.dto.EmployeeDTO;
import com.example.employee.dto.EmployeeSearchRequest;
import com.example.employee.exception.*;
import com.example.employee.repository.EmployeeRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("EmployeeService")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private DepartmentClient departmentClient;

    @InjectMocks
    private EmployeeService service;

    @Nested
    @DisplayName("Create Employee")
    class CreateEmployee {

        @ParameterizedTest(name = "create({0}) → duplicate? {1}")
        @CsvSource({
                "dina@example.com, false",
                "alice@example.com, true"
        })
        @DisplayName("handles email duplication correctly")
        void create_handles_duplicates(String email, boolean duplicate) {
            when(repository.existsByEmail(email)).thenReturn(duplicate);

            if (duplicate) {
                assertThatThrownBy(() -> service.create(EmployeeDTO.builder()
                        .firstName("X").lastName("Y").email(email).build(), null))
                        .isInstanceOf(DuplicateResourceException.class)
                        .hasMessageContaining("Employee already exists");
            } else {
                when(repository.save(any(Employee.class)))
                        .thenAnswer(inv -> { Employee e = inv.getArgument(0); e.setId(101L); return e; });

                var result = service.create(EmployeeDTO.builder()
                        .firstName("X").lastName("Y").email(email).build(), null);

                assertThat(result.getId()).isEqualTo(101L);
                assertThat(result.getEmail()).isEqualTo(email);
            }
        }

        @Test
        @DisplayName("validates department exists when provided")
        void create_validates_department_exists() {
            var dto = EmployeeDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .departmentId(999L)
                    .build();

            when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
            when(departmentClient.getDepartment(999L))
                    .thenThrow(FeignException.NotFound.class);

            assertThatThrownBy(() -> service.create(dto, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Department not found");
        }

        @Test
        @DisplayName("handles idempotency correctly")
        void create_handles_idempotency() {
            String idempotencyKey = "test-key-123";
            var dto = EmployeeDTO.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .email("jane@example.com")
                    .build();

            when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
            when(repository.save(any(Employee.class)))
                    .thenAnswer(inv -> { Employee e = inv.getArgument(0); e.setId(100L); return e; });

            // First call
            var result1 = service.create(dto, idempotencyKey);

            // Second call with same key should return cached result
            var result2 = service.create(dto, idempotencyKey);

            assertThat(result1).isEqualTo(result2);
            verify(repository, times(1)).save(any(Employee.class));
        }
    }

    @Nested
    @DisplayName("Get Employee")
    class GetEmployee {

        @Test
        @DisplayName("returns employee when found")
        void getById_returns_employee_when_found() {
            Long employeeId = 1L;
            Employee employee = Employee.builder()
                    .id(employeeId)
                    .firstName("Alice")
                    .lastName("Smith")
                    .email("alice@example.com")
                    .build();

            when(repository.findById(employeeId)).thenReturn(Optional.of(employee));

            var result = service.getById(employeeId);

            assertThat(result.getId()).isEqualTo(employeeId);
            assertThat(result.getFirstName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void getById_throws_when_not_found() {
            Long employeeId = 999L;
            when(repository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(employeeId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee not found with id: 999");
        }

        @Test
        @DisplayName("enriches employee with department data")
        void getById_enriches_with_department() {
            Long employeeId = 1L;
            Long departmentId = 10L;

            Employee employee = Employee.builder()
                    .id(employeeId)
                    .firstName("Bob")
                    .lastName("Johnson")
                    .email("bob@example.com")
                    .departmentId(departmentId)
                    .build();

            DepartmentDTO department = new DepartmentDTO();
            department.setId(departmentId);
            department.setName("Engineering");

            when(repository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(departmentClient.getDepartment(departmentId)).thenReturn(department);

            var result = service.getById(employeeId);

            assertThat(result.getDepartment()).isNotNull();
            assertThat(result.getDepartment().getName()).isEqualTo("Engineering");
        }

        @Test
        @DisplayName("handles department service failure gracefully")
        void getById_handles_department_service_failure() {
            Long employeeId = 1L;
            Employee employee = Employee.builder()
                    .id(employeeId)
                    .firstName("Charlie")
                    .lastName("Brown")
                    .email("charlie@example.com")
                    .departmentId(10L)
                    .build();

            when(repository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(departmentClient.getDepartment(10L))
                    .thenThrow(FeignException.InternalServerError.class);

            var result = service.getById(employeeId);

            assertThat(result.getDepartment()).isNull();
            assertThat(result.getDepartmentId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("Search Employee")
    class SearchEmployee {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("throws ValidationException for empty search query")
        void search_throws_for_empty_query(String query) {
            var pageable = PageRequest.of(0, 20);

            assertThatThrownBy(() -> service.search(query, pageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Search query cannot be empty");
        }

        @Test
        @DisplayName("returns matching employees")
        void search_returns_matching_employees() {
            String query = "alice";
            var pageable = PageRequest.of(0, 20);

            Employee employee = Employee.builder()
                    .id(1L)
                    .firstName("Alice")
                    .lastName("Smith")
                    .email("alice@example.com")
                    .build();

            Page<Employee> employeePage = new PageImpl<>(List.of(employee));
            when(repository.searchByNameOrEmail("alice", pageable)).thenReturn(employeePage);

            var result = service.search(query, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Alice");
        }
    }

    @Nested
    @DisplayName("Update Employee")
    class UpdateEmployee {

        @Test
        @DisplayName("updates employee successfully")
        void update_employee_successfully() {
            Long employeeId = 1L;
            Employee existing = Employee.builder()
                    .id(employeeId)
                    .firstName("Old")
                    .lastName("Name")
                    .email("old@example.com")
                    .build();

            EmployeeDTO updateDto = EmployeeDTO.builder()
                    .firstName("New")
                    .lastName("Name")
                    .email("new@example.com")
                    .build();

            when(repository.findById(employeeId)).thenReturn(Optional.of(existing));
            when(repository.existsByEmailAndIdNot("new@example.com", employeeId)).thenReturn(false);
            when(repository.save(any(Employee.class))).thenReturn(existing);

            var result = service.update(employeeId, updateDto);

            assertThat(result.getFirstName()).isEqualTo("New");
            assertThat(result.getEmail()).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("throws when employee not found")
        void update_throws_when_not_found() {
            Long employeeId = 999L;
            EmployeeDTO updateDto = EmployeeDTO.builder()
                    .firstName("New")
                    .lastName("Name")
                    .email("new@example.com")
                    .build();

            when(repository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(employeeId, updateDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Employee")
    class DeleteEmployee {

        @Test
        @DisplayName("deletes employee successfully")
        void delete_employee_successfully() {
            Long employeeId = 1L;
            when(repository.existsById(employeeId)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> service.delete(employeeId));

            verify(repository).deleteById(employeeId);
        }

        @Test
        @DisplayName("throws when employee not found")
        void delete_throws_when_not_found() {
            Long employeeId = 999L;
            when(repository.existsById(employeeId)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(employeeId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get All Employees")
    class GetAllEmployees {

        @Test
        @DisplayName("returns all employees when no filters")
        void getAll_returns_all_when_no_filters() {
            var pageable = PageRequest.of(0, 20);
            Page<Employee> employeePage = new PageImpl<>(List.of(
                    Employee.builder().id(1L).firstName("Alice").build(),
                    Employee.builder().id(2L).firstName("Bob").build()
            ));

            when(repository.findAll(pageable)).thenReturn(employeePage);

            var result = service.getAll(pageable, null);

            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("applies filters when provided")
        void getAll_applies_filters() {
            var pageable = PageRequest.of(0, 20);
            var filters = new EmployeeSearchRequest();
            filters.setEmail("alice");
            filters.setDepartmentId(1L);

            Page<Employee> employeePage = new PageImpl<>(List.of(
                    Employee.builder().id(1L).firstName("Alice").build()
            ));

            when(repository.findWithFilters("alice", null, 1L, pageable))
                    .thenReturn(employeePage);

            var result = service.getAll(pageable, filters);

            assertThat(result.getContent()).hasSize(1);
            verify(repository).findWithFilters("alice", null, 1L, pageable);
        }
    }
}