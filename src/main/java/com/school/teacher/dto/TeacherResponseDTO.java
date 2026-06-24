package com.school.teacher.dto;

import com.school.teacher.entity.Teacher.TeacherStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String department;
    private String designation;
    private LocalDate dateOfJoining;
    private String qualification;
    private Integer experienceYears;
    private TeacherStatus status;
    private List<String> subjectsTaught;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
