package com.school.teacher.service;

import com.school.teacher.dto.TeacherRequestDTO;
import com.school.teacher.dto.TeacherResponseDTO;
import com.school.teacher.entity.Teacher;
import org.springframework.stereotype.Component;

@Component
public class TeacherMapper {

    public Teacher toEntity(TeacherRequestDTO dto) {
        return Teacher.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .department(dto.getDepartment())
                .designation(dto.getDesignation())
                .dateOfJoining(dto.getDateOfJoining())
                .qualification(dto.getQualification())
                .experienceYears(dto.getExperienceYears())
                .status(dto.getStatus() != null ? dto.getStatus() : Teacher.TeacherStatus.ACTIVE)
                .subjectsTaught(dto.getSubjectsTaught())
                .build();
    }

    public TeacherResponseDTO toResponseDTO(Teacher teacher) {
        return TeacherResponseDTO.builder()
                .id(teacher.getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .fullName(teacher.getFirstName() + " " + teacher.getLastName())
                .email(teacher.getEmail())
                .phoneNumber(teacher.getPhoneNumber())
                .department(teacher.getDepartment())
                .designation(teacher.getDesignation())
                .dateOfJoining(teacher.getDateOfJoining())
                .qualification(teacher.getQualification())
                .experienceYears(teacher.getExperienceYears())
                .status(teacher.getStatus())
                .subjectsTaught(teacher.getSubjectsTaught())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDTO(Teacher teacher, TeacherRequestDTO dto) {
        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
        teacher.setPhoneNumber(dto.getPhoneNumber());
        teacher.setDepartment(dto.getDepartment());
        teacher.setDesignation(dto.getDesignation());
        teacher.setDateOfJoining(dto.getDateOfJoining());
        teacher.setQualification(dto.getQualification());
        teacher.setExperienceYears(dto.getExperienceYears());
        teacher.setStatus(dto.getStatus());
        teacher.setSubjectsTaught(dto.getSubjectsTaught());
    }
}
