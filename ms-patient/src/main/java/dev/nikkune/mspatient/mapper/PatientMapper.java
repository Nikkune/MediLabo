package dev.nikkune.mspatient.mapper;

import dev.nikkune.mspatient.dto.PatientDTO;
import dev.nikkune.mspatient.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper interface for converting between {@link PatientDTO} and {@link Patient} objects.
 * <p>
 * This interface is annotated with {@link Mapper} to indicate that it is a MapStruct mapper.
 * It provides methods for mapping the data transfer object (DTO) representation of a patient
 * to its corresponding entity representation and for updating existing patient entities with new DTO data.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PatientMapper {

    /**
     * Converts a {@link PatientDTO} object into a {@link Patient} entity.
     *
     * @param patientDTO the data transfer object containing patient details to be mapped to a Patient entity
     * @return a {@link Patient} entity containing the mapped properties of the given {@link PatientDTO}
     */
    Patient toPatient(PatientDTO patientDTO);

    PatientDTO toDTO(Patient patient);

    /**
     * Updates the fields of an existing Patient entity with the provided
     * data from the PatientDTO. Only non-null fields from the PatientDTO
     * will override the corresponding fields in the Patient entity.
     *
     * @param patientDTO the data transfer object containing the updated patient details
     * @param patient    the target Patient entity to be updated
     */
    void updatePatient(PatientDTO patientDTO, @MappingTarget Patient patient);
}
