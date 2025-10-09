package dev.nikkune.mspatient.controller;

import dev.nikkune.mspatient.dto.PatientDTO;
import dev.nikkune.mspatient.dto.RiskDTO;
import dev.nikkune.mspatient.model.Patient;
import dev.nikkune.mspatient.service.IPatientService;
import dev.nikkune.mspatient.validation.ValidationGroups;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PatientController is a REST controller that provides endpoints to perform
 * CRUD operations for Patient resources. It interacts with the underlying
 * PatientService to handle all business logic and database interactions.
 */
@RestController
@RequestMapping
@Validated
public class PatientController {
    private static final Logger logger = LogManager.getLogger(PatientController.class);
    private final IPatientService patientService;

    public PatientController(IPatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Retrieves all patients from the system.
     * <p>
     * This method handles an HTTP GET request and interacts with the PatientService
     * to fetch all the available patients from the underlying data store. It then
     * returns the list of patients wrapped in a ResponseEntity.
     *
     * @return a ResponseEntity containing a list of all patients
     */
    @GetMapping("/all")
    public List<PatientDTO> getAllPatients() {
        logger.debug("Received request to get all patients");
        List<PatientDTO> patients = patientService.findAll();
        logger.info("Retrieved {} patients", patients.size());
        return patients;
    }

    /**
     * Retrieves a patient by their unique ID.
     *
     * @param id the unique identifier of the patient to be retrieved
     * @return a {@link ResponseEntity} containing the {@link Patient} object if found
     */
    @GetMapping("/byId")
    public PatientDTO getPatientById(@RequestParam @Valid Integer id) {
        logger.debug("Received request to get patient with ID {}", id);
        PatientDTO patient = patientService.findById(id);
        logger.info("Retrieved patient with ID {}", id);
        return patient;
    }

    /**
     * Retrieves a patient based on their first name and last name.
     *
     * @param firstName the first name of the patient to be retrieved
     * @param lastName  the last name of the patient to be retrieved
     * @return a ResponseEntity containing the Patient object matching the provided first and last name
     */
    @GetMapping
    public PatientDTO getPatientByFirstNameAndLastName(@RequestParam @Valid String firstName, @RequestParam @Valid String lastName) {
        logger.debug("Received request to get patient with first name {} and last name {}", firstName, lastName);
        PatientDTO patient = patientService.findByFirstNameAndLastName(firstName, lastName);
        logger.info("Retrieved patient with first name {} and last name {}", firstName, lastName);
        return patient;
    }

    @GetMapping("/riskInfo")
    public RiskDTO getPatientRiskInfo(@RequestParam @Valid String firstName, @RequestParam @Valid String lastName) {
        logger.debug("Received request to get rist info of patient with first name {} and last name {}", firstName, lastName);
        RiskDTO riskInfo = patientService.getRiskInfo(firstName, lastName);
        logger.info("Retrieved birth rist info of patient with first name {} and last name {}", firstName, lastName);
        return riskInfo;
    }

    /**
     * Handles the creation of a new patient and saves it to the database.
     *
     * @param patient the Patient object to be created and saved, containing all necessary details
     *                about the patient
     * @return a ResponseEntity containing the saved Patient object and an HTTP status of 200 OK
     */
    @PostMapping
    public PatientDTO createPatient(@RequestBody @Validated({Default.class, ValidationGroups.Create.class}) PatientDTO patient) {
        logger.debug("Received request to create patient {}", patient);
        PatientDTO savedPatient = patientService.registerPatient(patient);
        logger.info("Created patient {}", savedPatient);
        return savedPatient;
    }

    /**
     * Updates the details of an existing patient.
     * <p>
     * This method receives a request to update a patient's information in the system.
     * The provided patient object must be valid and contain the updated details.
     *
     * @param patient the Patient object containing the updated information
     * @return a ResponseEntity containing the updated Patient object
     */
    @PutMapping
    public PatientDTO updatePatient(@RequestBody @Validated({Default.class, ValidationGroups.Update.class}) PatientDTO patient) {
        logger.debug("Received request to update patient {}", patient);
        PatientDTO updatedPatient = patientService.update(patient);
        logger.info("Updated patient {}", updatedPatient);
        return updatedPatient;
    }

    /**
     * Deletes a patient identified by their first and last name.
     * <p>
     * This method removes the patient record from the database or repository
     * with the specified first and last name, if it exists.
     *
     * @param firstName the first name of the patient to be deleted
     * @param lastName  the last name of the patient to be deleted
     * @return a ResponseEntity with no content, indicating successful deletion
     */
    @DeleteMapping
    public ResponseEntity<Void> deletePatient(@RequestParam @Valid String firstName, @RequestParam @Valid String lastName) {
        logger.debug("Received request to delete patient with first name {} and last name {}", firstName, lastName);
        patientService.delete(firstName, lastName);
        logger.info("Deleted patient with first name {} and last name {}", firstName, lastName);
        return ResponseEntity.ok().build();
    }
}
