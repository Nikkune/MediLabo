package dev.nikkune.mspatient.service;

import dev.nikkune.mspatient.dto.PatientDTO;
import dev.nikkune.mspatient.dto.RiskDTO;

import java.util.List;

public interface IPatientService {
    List<PatientDTO> findAll();

    PatientDTO findById(Integer id);

    PatientDTO findByFirstNameAndLastName(String firstName, String lastName);

    RiskDTO getRiskInfo(String firstName, String lastName);

    PatientDTO registerPatient(PatientDTO patient);

    PatientDTO update(PatientDTO patientDTO);

    void delete(String firstName, String lastName);
}
