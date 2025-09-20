package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class EmployeeStatsDTO {
    private Long totalEmployees;
    private Map<Long, Long> countsByDepartment;
}