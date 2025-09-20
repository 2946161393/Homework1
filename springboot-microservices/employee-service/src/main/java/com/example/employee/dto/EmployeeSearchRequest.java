package com.example.employee.dto;

import lombok.Data;

@Data
public class EmployeeSearchRequest {
    private String email;
    private String lastName;
    private Long departmentId;
    private String search; // for name/email search
}