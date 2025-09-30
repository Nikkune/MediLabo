package dev.nikkune.msnotes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String firstName, String lastName) {
        super("Patient not found: " + firstName + " " + lastName);
    }
}
