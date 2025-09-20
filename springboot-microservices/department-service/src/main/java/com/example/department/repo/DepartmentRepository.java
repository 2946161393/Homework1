package com.example.department.repo;

import com.example.department.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
    Optional<Department> findByCodeIgnoreCase(String code);

    @Query("SELECT d FROM Department d WHERE " +
            "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:code IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', :code, '%')))")
    Page<Department> findWithFilters(@Param("name") String name,
                                     @Param("code") String code,
                                     Pageable pageable);
}