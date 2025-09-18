package dev.nikkune.mspatient.validation;

/**
 * ValidationGroups is a utility class that defines validation group interfaces.
 * These interfaces can be used to categorize and apply conditional validation
 * rules for different operations such as creation or update of entities.
 */
public class ValidationGroups {
    public interface Create {}
    public interface Update {}
}
