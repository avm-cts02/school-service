package com.school.teacher.exception;

public class TeacherNotFoundException extends RuntimeException {

    public TeacherNotFoundException(Long id) {
        super("Teacher not found with id: " + id);
    }

    public TeacherNotFoundException(String message) {
        super(message);
    }
}
