package com.example.employee.service;

import com.example.employee.client.DepartmentClient;
import com.example.employee.domain.Employee;
import com.example.employee.dto.*;
import com.example.employee.exception.*;
import com.example.employee.repository.EmployeeRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;
    private final DepartmentClient departmentClient;

    // Simple in-memory store for idempotency keys
    private final Map<String, EmployeeDTO> idempotencyStore = new ConcurrentHashMap<>();

    public PageResponse<EmployeeDTO> getAll(Pageable pageable, EmployeeSearchRequest filters) {
        log.debug("Fetching employees with filters: {}", filters);

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
        log.debug("Fetching employee with id: {}", id);

        Employee e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return toDTO(e);
    }

    @Transactional
    public EmployeeDTO create(EmployeeDTO dto, String idempotencyKey) {
        log.info("Creating employee with email: {}", dto.getEmail());

        // Check idempotency key
        if (idempotencyKey != null && idempotencyStore.containsKey(idempotencyKey)) {
            log.debug("Returning cached result for idempotency key: {}", idempotencyKey);
            return idempotencyStore.get(idempotencyKey);
        }

        // Validate email uniqueness
        if (repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Employee", dto.getEmail());
        }

        // Validate department exists if provided
        if (dto.getDepartmentId() != null) {
            validateDepartmentExists(dto.getDepartmentId());
        }

        Employee e = Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .departmentId(dto.getDepartmentId())
                .build();

        e = repository.save(e);
        log.info("Created employee with id: {}", e.getId());

        EmployeeDTO result = toDTO(e);

        // Store for idempotency
        if (idempotencyKey != null) {
            idempotencyStore.put(idempotencyKey, result);
        }

        return result;
    }

    @Transactional
    public EmployeeDTO update(Long id, EmployeeDTO dto) {
        log.info("Updating employee with id: {}", id);

        Employee existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        // Validate email uniqueness
        if (repository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new DuplicateResourceException("Employee", dto.getEmail());
        }

        // Validate department exists if provided
        if (dto.getDepartmentId() != null) {
            validateDepartmentExists(dto.getDepartmentId());
        }

        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        existing.setDepartmentId(dto.getDepartmentId());

        existing = repository.save(existing);
        log.info("Updated employee with id: {}", existing.getId());

        return toDTO(existing);
    }

    @Transactional
    public EmployeeDTO patch(Long id, EmployeeDTO dto) {
        log.info("Patching employee with id: {}", id);

        Employee existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        if (dto.getFirstName() != null) {
            existing.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            existing.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null) {
            if (repository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                throw new DuplicateResourceException("Employee", dto.getEmail());
            }
            existing.setEmail(dto.getEmail());
        }
        if (dto.getDepartmentId() != null) {
            validateDepartmentExists(dto.getDepartmentId());
            existing.setDepartmentId(dto.getDepartmentId());
        }

        existing = repository.save(existing);
        log.info("Patched employee with id: {}", existing.getId());

        return toDTO(existing);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting employee with id: {}", id);

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }

        repository.deleteById(id);
        log.info("Deleted employee with id: {}", id);
    }

    public PageResponse<EmployeeDTO> search(String query, Pageable pageable) {
        log.debug("Searching employees with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException("Search query cannot be empty");
        }

        Page<Employee> page = repository.searchByNameOrEmail(query.trim(), pageable);
        Page<EmployeeDTO> dtoPage = page.map(this::toDTO);
        return PageResponse.from(dtoPage);
    }

    public EmployeeStatsDTO getStats() {
        log.debug("Fetching employee statistics");

        Long total = repository.countTotal();
        List<Object[]> deptCounts = repository.countByDepartment();

        Map<Long, Long> countsByDept = new HashMap<>();
        for (Object[] row : deptCounts) {
            countsByDept.put((Long) row[0], (Long) row[1]);
        }

        return new EmployeeStatsDTO(total, countsByDept);
    }

    private void validateDepartmentExists(Long departmentId) {
        try {
            departmentClient.getDepartment(departmentId);
            log.debug("Department {} exists", departmentId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Department", departmentId);
        } catch (FeignException e) {
            log.error("Error validating department {}: {}", departmentId, e.getMessage());
            throw new ExternalServiceException("department-service",
                    "Unable to validate department", e.status());
        } catch (Exception e) {
            log.error("Unexpected error validating department {}: {}", departmentId, e.getMessage(), e);
            throw new ExternalServiceException("department-service",
                    "Unexpected error during department validation", e);
        }
    }

    private EmployeeDTO toDTO(Employee e) {
        DepartmentDTO dept = null;
        if (e.getDepartmentId() != null) {
            try {
                dept = departmentClient.getDepartment(e.getDepartmentId());
                log.debug("Enriched employee {} with department {}", e.getId(), dept.getName());
            } catch (FeignException.NotFound ex) {
                log.warn("Department {} not found for employee {}", e.getDepartmentId(), e.getId());
            } catch (FeignException ex) {
                log.warn("Error fetching department {} for employee {}: {}",
                        e.getDepartmentId(), e.getId(), ex.getMessage());
            } catch (Exception ex) {
                log.error("Unexpected error fetching department {} for employee {}: {}",
                        e.getDepartmentId(), e.getId(), ex.getMessage(), ex);
            }
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