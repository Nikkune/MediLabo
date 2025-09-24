package dev.nikkune.mspatient.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * AddressValidator is a custom constraint validator that checks if a given String
 * represents a valid address format. It uses a regular expression to validate whether
 * the address conforms to a specific pattern, including a mandatory house number,
 * street name, and an acceptable suffix such as "Dr", "Road", "St", or others specified
 * in the {@code ValidAddress} annotation.
 * <p>
 * The pattern is dynamically generated during initialization based on the suffixes
 * provided in the {@code ValidAddress} annotation. This ensures flexibility in enforcing
 * different suffix requirements.
 * <p>
 * This class implements the {@code ConstraintValidator} interface and is designed to
 * be invoked during the validation process for fields or parameters annotated with
 * {@code ValidAddress}.
 * <p>
 * Methods:
 * - {@code initialize(ValidAddress constraintAnnotation)}: Compiles the regular expression
 * for validating addresses based on the suffixes provided in the annotation.
 * - {@code isValid(String value, ConstraintValidatorContext context)}: Validates whether
 * the provided value matches the compiled address pattern.
 */
public class AddressValidator implements ConstraintValidator<ValidAddress, String> {

    private Pattern pattern;


    @Override
    public void initialize(ValidAddress constraintAnnotation) {
        String joinedSuffixes = String.join("|", constraintAnnotation.suffixes());
        String regex = "^\\d+\\s+[A-Za-z]+(?:\\s+[A-Za-z]+)*\\s+(" + joinedSuffixes + ")$";
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Bean Validation best practice: constraints should be null-safe.
        // If the value is null or blank, leave responsibility to other constraints (e.g., @NotBlank).
        if (value == null || value.isBlank()) {
            return true;
        }
        return pattern.matcher(value).matches();
    }
}
