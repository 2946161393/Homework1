package com.example.department.client;

import com.example.department.dto.EmployeeDTO;
import com.example.department.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "EMPLOYEE-SERVICE", path = "/api/v1/employees")
public interface EmployeeClient {

    @GetMapping
    PageResponse<EmployeeDTO> getEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );
}