package com.school.teacher.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A teacher with email '" + email + "' already exists");
    }
}
