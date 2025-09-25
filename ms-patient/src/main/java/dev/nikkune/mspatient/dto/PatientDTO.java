package dev.nikkune.mspatient.dto;

import dev.nikkune.mspatient.validation.ValidAddress;
import dev.nikkune.mspatient.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

/**
 * Data Transfer Object (DTO) representing the details of a patient.
 * <p>
 * This class is used for transferring patient-related data between different layers
 * of the application, such as between controllers and services. It includes validation
 * rules to ensure the integrity of the data being processed, which are enforced using
 * annotations from the javax.validation.constraints package and custom annotations.
 * <p>
 * Fields in this class include commonly required information about a patient such as
 * their name, birth date, gender, address, and contact information. Validation rules
 * specify requirements like mandatory fields and acceptable formatting for certain
 * properties.
 * <p>
 * Field validations:
 * - `lastName`, `firstName`, `birthDate`, `gender`, `address`, and `phoneNumber`
 * are mandatory fields.
 * - `lastName` and `firstName` must be between 3 and 100 characters.
 * - `birthDate` must be in the past.
 * - `gender` must match the pattern "M" or "F".
 * - `address` must pass custom validation defined by the `@ValidAddress` annotation.
 * - `phoneNumber` must adhere to the format "xxx-xxx-xxxx".
 * <p>
 * This class leverages the Lombok `@Data` annotation to generate boilerplate code
 * such as getters, setters, toString, equals, and hashCode methods.
 */
@Data
public class PatientDTO {
    @NotBlank(message = "Last name must be provided")
    @Size(min = 3, max = 100)
    private String lastName;

    @NotBlank(message = "First name must be provided")
    @Size(min = 3, max = 100)
    private String firstName;

    @Past
    private Date birthDate;

    @NotBlank(message = "Gender must be provided", groups = ValidationGroups.Create.class)
    @Pattern(regexp = "^([MF])$", message = "Gender must be either M or F")
    private String gender;

    @ValidAddress
    private String address;

    @Pattern(regexp = "\\d{3}-\\d{3}-\\d{4}", message = "Phone number must be xxx-xxx-xxxx")
    private String phoneNumber;
}
