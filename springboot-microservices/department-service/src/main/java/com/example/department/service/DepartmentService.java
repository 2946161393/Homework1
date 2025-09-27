package com.example.department.service;

import com.example.department.client.EmployeeClient;
import com.example.department.domain.Department;
import com.example.department.dto.DepartmentDTO;
import com.example.department.dto.PageResponse;
import com.example.department.exception.*;
import com.example.department.repository.DepartmentRepository;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService")
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repository;

    @Mock
    private EmployeeClient employeeClient;

    @InjectMocks
    private DepartmentService service;

    @Nested
    @DisplayName("Create Department")
    class CreateDepartment {

        @Test
        @DisplayName("creates department successfully")
        void create_department_successfully() {
            DepartmentDTO dto = DepartmentDTO.builder()
                    .name("Engineering")
                    .code("ENG")
                    .description("Software development")
                    .build();

            when(repository.existsByCode("ENG")).thenReturn(false);
            when(repository.save(any(Department.class)))
                    .thenAnswer(inv -> { Department d = inv.getArgument(0); d.setId(1L); return d; });

            var result = service.create(dto);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCode()).isEqualTo("ENG");
        }

        @Test
        @DisplayName("throws DuplicateResourceException for existing code")
        void create_throws_for_duplicate_code() {
            DepartmentDTO dto = DepartmentDTO.builder()
                    .name("Engineering")
                    .code("ENG")
                    .build();

            when(repository.existsByCode("ENG")).thenReturn(true);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Department already exists");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "AB", "THIS_CODE_IS_WAY_TOO_LONG_FOR_VALIDATION"})
        @DisplayName("throws ValidationException for invalid codes")
        void create_throws_for_invalid_code(String invalidCode) {
            DepartmentDTO dto = DepartmentDTO.builder()
                    .name("Engineering")
                    .code(invalidCode)
                    .build();

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("Delete Department")
    class DeleteDepartment {

        @Test
        @DisplayName("deletes department successfully when no employees")
        void delete_department_successfully() {
            Long departmentId = 1L;
            PageResponse<Object> emptyResponse = new PageResponse<>();
            emptyResponse.setTotalElements(0);

            when(repository.existsById(departmentId)).thenReturn(true);
            when(employeeClient.getEmployees(departmentId, 0, 1)).thenReturn(emptyResponse);

            assertThatNoException().isThrownBy(() -> service.delete(departmentId));

            verify(repository).deleteById(departmentId);
        }

        @Test
        @DisplayName("throws DepartmentDeletionException when employees exist")
        void delete_throws_when_employees_exist() {
            Long departmentId = 1L;
            PageResponse<Object> responseWithEmployees = new PageResponse<>();
            responseWithEmployees.setTotalElements(5);

            when(repository.existsById(departmentId)).thenReturn(true);
            when(employeeClient.getEmployees(departmentId, 0, 1)).thenReturn(responseWithEmployees);

            assertThatThrownBy(() -> service.delete(departmentId))
                    .isInstanceOf(DepartmentDeletionException.class)
                    .hasMessageContaining("5 employee(s) are still assigned");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when department not found")
        void delete_throws_when_not_found() {
            Long departmentId = 999L;
            when(repository.existsById(departmentId)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(departmentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("handles employee service failure gracefully")
        void delete_handles_employee_service_failure() {
            Long departmentId = 1L;
            when(repository.existsById(departmentId)).thenReturn(true);
            when(employeeClient.getEmployees(departmentId, 0, 1))
                    .thenThrow(FeignException.InternalServerError.class);

            // Should proceed with deletion despite service failure
            assertThatNoException().isThrownBy(() -> service.delete(departmentId));

            verify(repository).deleteById(departmentId);
        }
    }

    @Nested
    @DisplayName("Get Department")
    class GetDepartment {

        @Test
        @DisplayName("returns department when found by id")
        void getById_returns_department() {
            Long departmentId = 1L;
            Department department = Department.builder()
                    .id(departmentId)
                    .name("Engineering")
                    .code("ENG")
                    .build();

            when(repository.findById(departmentId)).thenReturn(Optional.of(department));

            var result = service.getById(departmentId);

            assertThat(result.getId()).isEqualTo(departmentId);
            assertThat(result.getName()).isEqualTo("Engineering");
        }

        @Test
        @DisplayName("returns department when found by code")
        void getByCode_returns_department() {
            String code = "ENG";
            Department department = Department.builder()
                    .id(1L)
                    .name("Engineering")
                    .code(code)
                    .build();

            when(repository.findByCodeIgnoreCase(code)).thenReturn(Optional.of(department));

            var result = service.getByCode(code);

            assertThat(result.getCode()).isEqualTo(code);
            assertThat(result.getName()).isEqualTo("Engineering");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void getById_throws_when_not_found() {
            Long departmentId = 999L;
            when(repository.findById(departmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(departmentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Department not found with id: 999");
        }
    }

    @Nested
    @DisplayName("Update Department")
    class UpdateDepartment {

        @Test
        @DisplayName("updates department successfully")
        void update_department_successfully() {
            Long departmentId = 1L;
            Department existing = Department.builder()
                    .id(departmentId)
                    .name("Old Name")
                    .code("OLD")
                    .build();

            DepartmentDTO updateDto = DepartmentDTO.builder()
                    .name("New Name")
                    .code("NEW")
                    .description("Updated description")
                    .build();

            when(repository.findById(departmentId)).thenReturn(Optional.of(existing));
            when(repository.existsByCodeAndIdNot("NEW", departmentId)).thenReturn(false);
            when(repository.save(any(Department.class))).thenReturn(existing);

            var result = service.update(departmentId, updateDto);

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getCode()).isEqualTo("NEW");
        }

        @Test
        @DisplayName("throws when department not found")
        void update_throws_when_not_found() {
            Long departmentId = 999L;
            DepartmentDTO updateDto = DepartmentDTO.builder()
                    .name("New Name")
                    .code("NEW")
                    .build();

            when(repository.findById(departmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(departmentId, updateDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "test@", "@example.com", "test.example.com"})
        @DisplayName("throws ValidationException for invalid email formats")
        void patch_throws_for_invalid_email(String invalidEmail) {
            Long departmentId = 1L;
            Department existing = Department.builder()
                    .id(departmentId)
                    .name("Engineering")
                    .code("ENG")
                    .build();

            DepartmentDTO patchDto = DepartmentDTO.builder()
                    .managerEmail(invalidEmail)
                    .build();

            when(repository.findById(departmentId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> service.patch(departmentId, patchDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid manager email format");
        }

        @Test
        @DisplayName("accepts valid email formats")
        void patch_accepts_valid_email() {
            Long departmentId = 1L;
            Department existing = Department.builder()
                    .id(departmentId)
                    .name("Engineering")
                    .code("ENG")
                    .build();

            DepartmentDTO patchDto = DepartmentDTO.builder()
                    .managerEmail("manager@example.com")
                    .build();

            when(repository.findById(departmentId)).thenReturn(Optional.of(existing));
            when(repository.save(any(Department.class))).thenReturn(existing);

            assertThatNoException().isThrownBy(() -> service.patch(departmentId, patchDto));
        }
    }
}