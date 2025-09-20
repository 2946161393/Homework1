package com.example.department.service;

import com.example.department.client.EmployeeClient;
import com.example.department.domain.Department;
import com.example.department.dto.DepartmentDTO;
import com.example.department.dto.EmployeeDTO;
import com.example.department.dto.PageResponse;
import com.example.department.repo.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository repository;
    private final EmployeeClient employeeClient;

    public PageResponse<DepartmentDTO> getAll(Pageable pageable, String name, String code) {
        Page<Department> page = repository.findWithFilters(name, code, pageable);
        Page<DepartmentDTO> dtoPage = page.map(this::toDTO);
        return PageResponse.from(dtoPage);
    }

    public DepartmentDTO getById(Long id) {
        Department dept = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));
        return toDTO(dept);
    }

    public DepartmentDTO getByCode(String code) {
        Department dept = repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with code: " + code));
        return toDTO(dept);
    }

    @Transactional
    public DepartmentDTO create(DepartmentDTO dto) {
        if (repository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Department code already exists: " + dto.getCode());
        }

        Department dept = Department.builder()
                .name(dto.getName())
                .code(dto.getCode().toUpperCase())
                .description(dto.getDescription())
                .managerEmail(dto.getManagerEmail())
                .build();

        dept = repository.save(dept);
        return toDTO(dept);
    }

    @Transactional
    public DepartmentDTO update(Long id, DepartmentDTO dto) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));

        if (repository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Department code already exists: " + dto.getCode());
        }

        existing.setName(dto.getName());
        existing.setCode(dto.getCode().toUpperCase());
        existing.setDescription(dto.getDescription());
        existing.setManagerEmail(dto.getManagerEmail());

        existing = repository.save(existing);
        return toDTO(existing);
    }

    @Transactional
    public DepartmentDTO patch(Long id, DepartmentDTO dto) {
        Department existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getCode() != null) {
            if (repository.existsByCodeAndIdNot(dto.getCode(), id)) {
                throw new IllegalArgumentException("Department code already exists: " + dto.getCode());
            }
            existing.setCode(dto.getCode().toUpperCase());
        }
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getManagerEmail() != null) existing.setManagerEmail(dto.getManagerEmail());

        existing = repository.save(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Department not found with id: " + id);
        }

        // Check if any employees are assigned to this department
        try {
            PageResponse<EmployeeDTO> employees = employeeClient.getEmployees(id, 0, 1);
            if (employees.getTotalElements() > 0) {
                throw new IllegalArgumentException(
                        "Cannot delete department. " + employees.getTotalElements() +
                                " employee(s) are still assigned to this department. " +
                                "Please reassign or remove employees first."
                );
            }
        } catch (Exception e) {
            // If employee service is down, allow deletion but log warning
            // In production, you might want to be more strict about this
            System.err.println("Warning: Unable to check employees for department " + id + ": " + e.getMessage());
        }

        repository.deleteById(id);
    }

    public PageResponse<EmployeeDTO> getDepartmentEmployees(Long departmentId, Pageable pageable) {
        // Verify department exists
        if (!repository.existsById(departmentId)) {
            throw new EntityNotFoundException("Department not found with id: " + departmentId);
        }

        try {
            return employeeClient.getEmployees(departmentId, pageable.getPageNumber(), pageable.getPageSize());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch employees for department: " + departmentId, e);
        }
    }

    private DepartmentDTO toDTO(Department dept) {
        return DepartmentDTO.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .managerEmail(dept.getManagerEmail())
                .build();
    }
}