package com.example.department;

import com.example.department.domain.Department;
import com.example.department.repo.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {
    @Mock
    DepartmentRepository repository;

    @Test
    void findAll_returns_departments() {
        when(repository.findAll()).thenReturn(List.of(
                Department.builder().id(1L).name("Engineering").build()
        ));

        var result = repository.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Engineering");
    }
}