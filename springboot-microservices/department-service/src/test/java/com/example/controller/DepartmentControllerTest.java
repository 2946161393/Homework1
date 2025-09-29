package com.example.controller;

import com.example.department.controller.DepartmentController;
import com.example.department.domain.Department;
import com.example.department.repository.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean DepartmentRepository repository;

    @Test
    void list_returns_200() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
                Department.builder().id(1L).name("Engineering").build()
        ));

        mvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Engineering"));
    }

    @Test
    void create_valid_returns_201() throws Exception {
        var dept = Department.builder().name("Finance").description("Money things").build();
        var saved = Department.builder().id(1L).name("Finance").description("Money things").build();

        when(repository.save(any(Department.class))).thenReturn(saved);

        mvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dept)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }
}