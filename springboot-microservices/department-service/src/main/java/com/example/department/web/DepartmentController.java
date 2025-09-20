package com.example.department.web;

import com.example.department.dto.DepartmentDTO;
import com.example.department.dto.EmployeeDTO;
import com.example.department.dto.PageResponse;
import com.example.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService service;

    @GetMapping
    public PageResponse<DepartmentDTO> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {

        Pageable pageable = createPageable(page, size, sort);
        return service.getAll(pageable, name, code);
    }

    @GetMapping("/{id}")
    public DepartmentDTO byId(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/by-code/{code}")
    public DepartmentDTO byCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentDTO create(@Valid @RequestBody DepartmentDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public DepartmentDTO update(@PathVariable Long id, @Valid @RequestBody DepartmentDTO dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}")
    public DepartmentDTO patch(@PathVariable Long id, @RequestBody DepartmentDTO dto) {
        return service.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/employees")
    public PageResponse<EmployeeDTO> getDepartmentEmployees(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        return service.getDepartmentEmployees(id, pageable);
    }

    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String field = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}