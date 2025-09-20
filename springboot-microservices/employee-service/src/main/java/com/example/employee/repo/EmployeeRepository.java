package com.example.employee.repo;

import com.example.employee.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    Page<Employee> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Employee> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
            "(:email IS NULL OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:departmentId IS NULL OR e.departmentId = :departmentId)")
    Page<Employee> findWithFilters(@Param("email") String email,
                                   @Param("lastName") String lastName,
                                   @Param("departmentId") Long departmentId,
                                   Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employee> searchByNameOrEmail(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e")
    Long countTotal();

    @Query("SELECT e.departmentId, COUNT(e) FROM Employee e WHERE e.departmentId IS NOT NULL GROUP BY e.departmentId")
    List<Object[]> countByDepartment();
}