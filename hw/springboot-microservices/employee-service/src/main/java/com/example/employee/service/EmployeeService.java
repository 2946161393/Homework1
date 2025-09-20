package com.example.employee.service;

import com.example.employee.client.DepartmentClient;
import com.example.employee.domain.Employee;
import com.example.employee.dto.*;
import com.example.employee.repo.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;
    private final DepartmentClient departmentClient;

    // Simple in-memory store for idempotency keys
    private final Map<String, EmployeeDTO> idempotencyStore = new ConcurrentHashMap<>();

    public PageResponse<EmployeeDTO> getAll(Pageable pageable, EmployeeSearchRequest filters) {
        Page<Employee> page;

        if (filters != null && hasFilters(filters)) {
            page = repository.findWithFilters(
                    filters.getEmail(),
                    filters.getLastName(),
                    filters.getDepartmentId(),
                    pageable
            );
        } else {
            page = repository.findAll(pageable);
        }

        Page<EmployeeDTO> dtoPage = page.map(this::toDTO);
        return PageResponse.from(dtoPage);
    }

    private boolean hasFilters(EmployeeSearchRequest filters) {
        return filters.getEmail() != null ||
                filters.getLastName() != null ||
                filters.getDepartmentId() != null;
    }

    public EmployeeDTO getById(Long id) {
        Employee e = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
        return toDTO(e);
    }

    @Transactional
    public EmployeeDTO create(EmployeeDTO dto, String idempotencyKey) {
        // Check idempotency key
        if (idempotencyKey != null && idempotencyStore.containsKey(idempotencyKey)) {
            return idempotencyStore.get(idempotencyKey);
        }

        if (repository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        Employee e = Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .departmentId(dto.getDepartmentId())
                .build();
        e = repository.save(e);

        EmployeeDTO result = toDTO(e);

        // Store for idempotency
        if (idempotencyKey != null) {
            idempotencyStore.put(idempotencyKey, result);
        }

        return result;
    }

    @Transactional
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        Employee existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

        if (repository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        existing.setDepartmentId(dto.getDepartmentId());

        existing = repository.save(existing);
        return toDTO(existing);
    }

    @Transactional
    public EmployeeDTO patch(Long id, EmployeeDTO dto) {
        Employee existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

        if (dto.getFirstName() != null) existing.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) existing.setLastName(dto.getLastName());
        if (dto.getEmail() != null) {
            if (repository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
            }
            existing.setEmail(dto.getEmail());
        }
        if (dto.getDepartmentId() != null) existing.setDepartmentId(dto.getDepartmentId());

        existing = repository.save(existing);
        return toDTO(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Employee not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public PageResponse<EmployeeDTO> search(String query, Pageable pageable) {
        Page<Employee> page = repository.searchByNameOrEmail(query, pageable);
        Page<EmployeeDTO> dtoPage = page.map(this::toDTO);
        return PageResponse.from(dtoPage);
    }

    public EmployeeStatsDTO getStats() {
        Long total = repository.countTotal();
        List<Object[]> deptCounts = repository.countByDepartment();

        Map<Long, Long> countsByDept = new HashMap<>();
        for (Object[] row : deptCounts) {
            countsByDept.put((Long) row[0], (Long) row[1]);
        }

        return new EmployeeStatsDTO(total, countsByDept);
    }

    private EmployeeDTO toDTO(Employee e) {
        DepartmentDTO dept = null;
        if (e.getDepartmentId() != null) {
            try {
                dept = departmentClient.getDepartment(e.getDepartmentId());
            } catch (Exception ignored) { }
        }
        return EmployeeDTO.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .departmentId(e.getDepartmentId())
                .department(dept)
                .build();
    }
}