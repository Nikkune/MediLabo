package dev.nikkune.mspatient.service;

import dev.nikkune.mspatient.dto.PatientDTO;
import dev.nikkune.mspatient.mapper.PatientMapper;
import dev.nikkune.mspatient.model.Patient;
import dev.nikkune.mspatient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for managing patient-related operations.
 * <p>
 * This class acts as an intermediary between the controller layer and the data access layer,
 * providing methods to perform CRUD (Create, Read, Update, Delete) operations for Patient entities.
 */
@Service
public class PatientService implements IPatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper mapper;

    private PatientService(PatientRepository patientRepository, PatientMapper mapper) {
        this.patientRepository = patientRepository;
        this.mapper = mapper;
    }

    /**
     * Retrieves a list of all patients from the repository.
     *
     * @return a list of all {@link Patient} entities stored in the repository
     */
    @Override
    public List<PatientDTO> findAll() {
        List<Patient> patients = patientRepository.findAllByActiveTrue();
        return patients.stream().map(mapper::toDTO).toList();
    }

    /**
     * Retrieves a patient identified by their unique ID.
     *
     * @param id the unique identifier of the patient to be retrieved
     * @return the Patient object if found, or null if no patient exists with the specified ID
     */
    @Override
    public PatientDTO findById(Integer id) {
        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient == null || patient.getActive() == false)
            throw new RuntimeException("Patient with ID " + id + " does not exist");
        return mapper.toDTO(patient);
    }

    /**
     * Finds a patient by their first name and last name.
     *
     * @param firstName the first name of the patient
     * @param lastName  the last name of the patient
     * @return the Patient object matching the given first and last name, or null if not found
     */
    @Override
    public PatientDTO findByFirstNameAndLastName(String firstName, String lastName) {
        Patient patient = patientRepository.findByFirstNameAndLastNameAndActiveTrue(firstName, lastName);
        if (patient == null)
            throw new RuntimeException("Patient with first name " + firstName + " and last name " + lastName + " does not exist");
        return mapper.toDTO(patient);
    }

    /**
     * Registers a new patient in the system.
     * <p>
     * This method converts the provided PatientRegistrationDTO into a Patient entity,
     * marks the entity as active, and persists it in the repository.
     *
     * @param patient the PatientRegistrationDTO containing the details of the patient to be registered
     * @return the saved Patient entity after registration
     */
    @Override
    public PatientDTO registerPatient(PatientDTO patient) {
        Patient patientEntity = mapper.toPatient(patient);
        if (patientRepository.findByFirstNameAndLastNameAndActiveTrue(patientEntity.getFirstName(), patientEntity.getLastName()) != null) {
            throw new RuntimeException("Patient with first name " + patientEntity.getFirstName() + " and last name " + patientEntity.getLastName() + " already exists");
        }
        patientEntity.setActive(true);
        Patient registeredPatient = patientRepository.save(patientEntity);
        return mapper.toDTO(registeredPatient);
    }

    /**
     * Updates the information of an existing patient identified by their unique ID.
     * <p>
     * This method retrieves the patient using the provided ID. If the patient exists,
     * it updates the patient entity with the new values from the provided PatientDTO
     * and saves the updated entity to the repository. If the patient does not exist,
     * a RuntimeException is thrown.
     *
     * @param patientDTO the data transfer object containing the new patient details
     * @return the updated {@link Patient} entity after saving to the repository
     * @throws RuntimeException if no patient is found with the given ID
     */
    @Override
    public PatientDTO update(PatientDTO patientDTO) {
        String firstName = patientDTO.getFirstName();
        String lastName = patientDTO.getLastName();
        Patient patient = patientRepository.findByFirstNameAndLastNameAndActiveTrue(firstName, lastName);
        if (patient == null)
            throw new RuntimeException("Patient with first name " + firstName + " and last name " + lastName + " does not exist");

        mapper.updatePatient(patientDTO, patient);

        Patient updatedPatient = patientRepository.save(patient);

        return mapper.toDTO(updatedPatient);
    }

    /**
     * Deletes a patient entity from the repository by its unique identifier.
     *
     * @param firstName the first name of the patient
     * @param lastName  the last name of the patient
     */
    @Override
    public void delete(String firstName, String lastName) {
        Patient patient = patientRepository.findByFirstNameAndLastNameAndActiveTrue(firstName, lastName);
        if (patient == null)
            throw new RuntimeException("Patient with first name " + firstName + " and last name " + lastName + " does not exist");

        patient.setActive(false);

        patientRepository.save(patient);
    }
}
