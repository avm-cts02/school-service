package com.school.teacher.config;

import com.school.teacher.entity.Teacher;
import com.school.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TeacherRepository teacherRepository;

    @Override
    public void run(String... args) {
        if (teacherRepository.count() == 0) {
            log.info("Initializing sample teacher data...");
            List<Teacher> sampleTeachers = List.of(
                Teacher.builder()
                    .firstName("Priya").lastName("Sharma")
                    .email("priya.sharma@school.com").phoneNumber("9876543210")
                    .department("Mathematics").designation("Senior Teacher")
                    .dateOfJoining(LocalDate.of(2018, 6, 1))
                    .qualification("M.Sc Mathematics").experienceYears(8)
                    .status(Teacher.TeacherStatus.ACTIVE)
                    .subjectsTaught(List.of("Algebra", "Calculus", "Statistics"))
                    .build(),
                Teacher.builder()
                    .firstName("Ravi").lastName("Kumar")
                    .email("ravi.kumar@school.com").phoneNumber("9876543211")
                    .department("Science").designation("Head of Department")
                    .dateOfJoining(LocalDate.of(2015, 7, 15))
                    .qualification("M.Sc Physics, B.Ed").experienceYears(12)
                    .status(Teacher.TeacherStatus.ACTIVE)
                    .subjectsTaught(List.of("Physics", "Chemistry"))
                    .build(),
                Teacher.builder()
                    .firstName("Ananya").lastName("Patel")
                    .email("ananya.patel@school.com").phoneNumber("9876543212")
                    .department("English").designation("Teacher")
                    .dateOfJoining(LocalDate.of(2020, 3, 10))
                    .qualification("M.A English Literature").experienceYears(4)
                    .status(Teacher.TeacherStatus.ON_LEAVE)
                    .subjectsTaught(List.of("English Grammar", "Literature"))
                    .build()
            );
            teacherRepository.saveAll(sampleTeachers);
            log.info("Sample data initialized with {} teachers.", sampleTeachers.size());
        }
    }
}
