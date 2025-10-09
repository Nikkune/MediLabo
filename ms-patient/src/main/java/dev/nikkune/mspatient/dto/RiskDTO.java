package dev.nikkune.mspatient.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RiskDTO {
    private Date birthDate;
    private String gender;
}
