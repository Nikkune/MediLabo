package dev.nikkune.mspatient.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * Represents a patient in the system.
 * <p>
 * A Patient object contains personal information including name, birth date,
 * gender, contact details, and other relevant attributes for identifying and managing
 * patients in a healthcare or related system.
 */
@Data
@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private Date birthDate;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean active;
}