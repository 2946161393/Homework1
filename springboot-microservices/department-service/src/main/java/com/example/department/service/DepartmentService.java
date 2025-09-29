package com.example.department.service;

public class DepartmentService {
}
package com.example.department.service;

import com.example.department.client.EmployeeClient;
import com.example.department.domain.Department;
import com.example.department.dto.DepartmentDTO;
import com.example.department.dto.EmployeeDTO;
import com.example.department.dto.PageResponse;
import com.example.department.repository.DepartmentRepository;

import com.example.exception.common.BusinessException;
import com.example.exception.common.ValidationException;
import com.example.exception.resource.DuplicateResourceException;
import com.example.exception.resource.ResourceNotFoundException;
import com.example.exception.external.ExternalServiceException;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Department Service
 * Handles business logic for department operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository repository;
    private final EmployeeClient employeeClient;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Get all departments with pagination and filters
     */
    public PageResponse<DepartmentDTO> getAll(Pageable pageable, String name, String code) {
        log.debug("Fetching departments with filters - name: {}, code: {}", name, code);

        Page<Department> page = repository.findWithFilters(name, code, pageable);
        Page<DepartmentDTO> dtoPage = page.map(this::toDTO);

        return PageResponse.from(dtoPage);
    }

    /**
     * Get department by ID
     */
    public DepartmentDTO getById(Long id) {
        log.debug("Fetching department with id: {}", id);

        Department department = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        return toDTO(department);
    }

    /**
     * Get department by code (case-insensitive)
     */
    public DepartmentDTO getByCode(String code) {
        log.debug("Fetching department with code: {}", code);

        Department department = repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Department not found with code: %s", code)));

        return toDTO(department);
    }

    /**
     * Create new department
     */
    @Transactional
    public DepartmentDTO create(DepartmentDTO dto) {
        log.info("Creating department with code: {}", dto.getCode());

        // Validate code format
        validateCode(dto.getCode());

        // Check for duplicate code
        if (repository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("Department", dto.getCode());
        }

        // Validate manager email if provided
        if (dto.getManagerEmail() != null && !dto.getManagerEmail().trim().isEmpty()) {
            validateEmail(dto.getManagerEmail());
        }

        Department department = Department.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .managerEmail(dto.getManagerEmail())
                .build();

        department = repository.save(department);
        log.info("Created department with id: {}", department.getId());

        return toDTO(department);
    }

    /**
     * Update existing department
     */
    @Transactional
    public DepartmentDTO update(Long id, DepartmentDTO dto) {
        log.info("Updating department with id: {}", id);

        Department existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        // Validate code format
        validateCode(dto.getCode());

        // Check for duplicate code (excluding current department)
        if (repository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw new DuplicateResourceException("Department", dto.getCode());
        }

        // Validate manager email if provided
        if (dto.getManagerEmail() != null && !dto.getManagerEmail().trim().isEmpty()) {
            validateEmail(dto.getManagerEmail());
        }

        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        existing.setDescription(dto.getDescription());
        existing.setManagerEmail(dto.getManagerEmail());

        existing = repository.save(existing);
        log.info("Updated department with id: {}", existing.getId());

        return toDTO(existing);
    }

    /**
     * Partially update department (PATCH)
     */
    @Transactional
    public DepartmentDTO patch(Long id, DepartmentDTO dto) {
        log.info("Patching department with id: {}", id);

        Department existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }

        if (dto.getCode() != null) {
            validateCode(dto.getCode());
            if (repository.existsByCodeAndIdNot(dto.getCode(), id)) {
                throw new DuplicateResourceException("Department", dto.getCode());
            }
            existing.setCode(dto.getCode());
        }

        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }

        if (dto.getManagerEmail() != null) {
            if (!dto.getManagerEmail().trim().isEmpty()) {
                validateEmail(dto.getManagerEmail());
            }
            existing.setManagerEmail(dto.getManagerEmail());
        }

        existing = repository.save(existing);
        log.info("Patched department with id: {}", existing.getId());

        return toDTO(existing);
    }

    /**
     * Delete department (only if no employees assigned)
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting department with id: {}", id);

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Department", id);
        }

        // Check if department has employees
        try {
            PageResponse<EmployeeDTO> employees = employeeClient.getEmployees(id, 0, 1);
            if (employees.getTotalElements() > 0) {
                throw new BusinessException(
                        String.format("Cannot delete department %d: %d employee(s) are still assigned to this department",
                                id, employees.getTotalElements()),
                        "DEPARTMENT_HAS_EMPLOYEES");
            }
        } catch (FeignException.NotFound e) {
            // Department has no employees, safe to delete
            log.debug("No employees found for department {}", id);
        } catch (FeignException e) {
            // If employee service is down, log warning but proceed with deletion
            log.warn("Could not verify employees for department {}: {}. Proceeding with deletion.",
                    id, e.getMessage());
        }

        repository.deleteById(id);
        log.info("Deleted department with id: {}", id);
    }

    /**
     * Get employees in a department
     */
    public PageResponse<EmployeeDTO> getDepartmentEmployees(Long departmentId, Pageable pageable) {
        log.debug("Fetching employees for department: {}", departmentId);

        // Verify department exists
        if (!repository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", departmentId);
        }

        try {
            return employeeClient.getEmployees(departmentId, pageable.getPageNumber(), pageable.getPageSize());
        } catch (FeignException.NotFound e) {
            log.warn("No employees found for department {}", departmentId);
            return new PageResponse<>();
        } catch (FeignException e) {
            log.error("Error fetching employees for department {}: {}", departmentId, e.getMessage());
            throw new ExternalServiceException("employee-service",
                    "Unable to fetch employees", e.status());
        }
    }

    /**
     * Validate department code format
     */
    private void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("code", "Department code cannot be empty");
        }

        if (code.length() < 3 || code.length() > 20) {
            throw new ValidationException("code", "Department code must be between 3 and 20 characters");
        }

        // Optional: Validate code format (alphanumeric with hyphens/underscores)
        if (!code.matches("^[A-Za-z0-9_-]+$")) {
            throw new ValidationException("code", "Department code can only contain letters, numbers, hyphens, and underscores");
        }
    }

    /**
     * Validate email format
     */
    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("managerEmail", "Invalid manager email format");
        }
    }

    /**
     * Convert entity to DTO
     */
    private DepartmentDTO toDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .managerEmail(department.getManagerEmail())
                .build();
    }
}