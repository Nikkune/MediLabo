package dev.nikkune.msrisk.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PatientDTO {
    private String lastName;
    private String firstName;
    private Date birthDate;
    private String gender;
    private String address;
    private String phoneNumber;
}
