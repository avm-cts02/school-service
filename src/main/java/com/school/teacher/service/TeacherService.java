package com.school.teacher.service;

import com.school.teacher.dto.PagedResponseDTO;
import com.school.teacher.dto.TeacherRequestDTO;
import com.school.teacher.dto.TeacherResponseDTO;
import com.school.teacher.entity.Teacher.TeacherStatus;
import org.springframework.data.domain.Pageable;

public interface TeacherService {

    TeacherResponseDTO createTeacher(TeacherRequestDTO requestDTO);

    TeacherResponseDTO getTeacherById(Long id);

    TeacherResponseDTO getTeacherByEmail(String email);

    PagedResponseDTO<TeacherResponseDTO> getAllTeachers(Pageable pageable);

    PagedResponseDTO<TeacherResponseDTO> getTeachersByDepartment(String department, Pageable pageable);

    PagedResponseDTO<TeacherResponseDTO> getTeachersByStatus(TeacherStatus status, Pageable pageable);

    PagedResponseDTO<TeacherResponseDTO> searchTeachers(String keyword, Pageable pageable);

    TeacherResponseDTO updateTeacher(Long id, TeacherRequestDTO requestDTO);

    TeacherResponseDTO updateTeacherStatus(Long id, TeacherStatus status);

    void deleteTeacher(Long id);
}
