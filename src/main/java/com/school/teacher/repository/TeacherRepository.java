package com.school.teacher.repository;

import com.school.teacher.entity.Teacher;
import com.school.teacher.entity.Teacher.TeacherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Page<Teacher> findByDepartment(String department, Pageable pageable);

    Page<Teacher> findByStatus(TeacherStatus status, Pageable pageable);

    @Query("SELECT t FROM Teacher t WHERE " +
           "LOWER(t.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Teacher> searchTeachers(@Param("keyword") String keyword, Pageable pageable);
}
