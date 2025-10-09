package dev.nikkune.mspatient.repository;

import dev.nikkune.mspatient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Repository interface for performing CRUD and custom operations on {@link Patient} entities.
 * <p>
 * This interface extends {@link JpaRepository} for basic CRUD operations and pagination support,
 * and {@link JpaSpecificationExecutor} for executing specification-based queries.
 */
public interface PatientRepository extends JpaRepository<Patient, Integer>, JpaSpecificationExecutor<Patient> {
    List<Patient> findAllByActiveTrue();

    Patient findByFirstNameAndLastNameAndActiveTrue(String firstName, String lastName);
}
