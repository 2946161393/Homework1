package com.example.employee.web;

import com.example.employee.dto.*;
import com.example.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping
    public PageResponse<EmployeeDTO> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Long departmentId) {

        Pageable pageable = createPageable(page, size, sort);
        EmployeeSearchRequest filters = new EmployeeSearchRequest();
        filters.setEmail(email);
        filters.setLastName(lastName);
        filters.setDepartmentId(departmentId);

        return service.getAll(pageable, filters);
    }

    @GetMapping("/{id}")
    public EmployeeDTO byId(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDTO create(@Valid @RequestBody EmployeeDTO dto,
                              @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.create(dto, idempotencyKey);
    }

    @PutMapping("/{id}")
    public EmployeeDTO update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}")
    public EmployeeDTO patch(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        return service.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/search")
    public PageResponse<EmployeeDTO> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        return service.search(q, pageable);
    }

    @GetMapping("/stats")
    public EmployeeStatsDTO stats() {
        return service.getStats();
    }

    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String field = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}