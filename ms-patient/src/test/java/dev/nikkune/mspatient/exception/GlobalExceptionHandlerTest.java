package dev.nikkune.mspatient.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_shouldReturn404_whenNotFoundMessage() {
        RuntimeException ex = new RuntimeException("Resource does not exist");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Not found", response.getBody().get("message"));
    }

    @Test
    void handleRuntimeException_shouldReturn409_whenAlreadyExistsMessage() {
        RuntimeException ex = new RuntimeException("Patient already exists");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Conflict", response.getBody().get("message"));
    }

    @Test
    void handleRuntimeException_shouldReturn400_forOtherMessages() {
        RuntimeException ex = new RuntimeException("Some other error");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody().get("message"));
    }

    @Test
    void handleGenericException_shouldReturn500() {
        Exception ex = new Exception("Boom");
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Internal server error", response.getBody().get("message"));
    }

    @Test
    void handleValidationExceptions_shouldExtractFieldNames_fromConstraintViolationException() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        DummyBean bean = new DummyBean(); // name is null -> violates @NotBlank
        Set<ConstraintViolation<DummyBean>> violations = validator.validate(bean);
        assertFalse(violations.isEmpty());
        // Wrap violations in the exception as Spring would during validation
        ConstraintViolationException cve = new ConstraintViolationException("validation failed", new HashSet<>(violations));

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(cve);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("Bad request", response.getBody().get("message"));
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertTrue(errors.containsKey("name"));
        assertNotNull(errors.get("name"));
    }

    static class DummyBean {
        @NotBlank(message = "must not be blank")
        String name;
    }
}
