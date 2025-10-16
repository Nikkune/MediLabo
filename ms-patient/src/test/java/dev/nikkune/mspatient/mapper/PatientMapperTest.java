package dev.nikkune.mspatient.mapper;

import dev.nikkune.mspatient.dto.PatientDTO;
import dev.nikkune.mspatient.model.Patient;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PatientMapperTest {

    private final PatientMapper mapper = Mappers.getMapper(PatientMapper.class);

    @Test
    void toPatient_shouldMapAllFields() {
        PatientDTO dto = new PatientDTO();
        dto.setLastName("Doe");
        dto.setFirstName("John");
        dto.setBirthDate(new Date(0));
        dto.setGender("M");
        dto.setAddress("123 Main St");
        dto.setPhoneNumber("123-456-7890");

        Patient entity = mapper.toPatient(dto);

        assertNotNull(entity);
        assertEquals("Doe", entity.getLastName());
        assertEquals("John", entity.getFirstName());
        assertEquals(new Date(0), entity.getBirthDate());
        assertEquals("M", entity.getGender());
        assertEquals("123 Main St", entity.getAddress());
        assertEquals("123-456-7890", entity.getPhoneNumber());
    }

    @Test
    void toDTO_shouldMapAllFields() {
        Patient entity = new Patient();
        entity.setId(1);
        entity.setLastName("Doe");
        entity.setFirstName("Jane");
        entity.setBirthDate(new Date(1000));
        entity.setGender("F");
        entity.setAddress("456 Oak Ave");
        entity.setPhoneNumber("111-222-3333");
        entity.setActive(true);

        PatientDTO dto = mapper.toDTO(entity);

        assertNotNull(dto);
        assertEquals("Doe", dto.getLastName());
        assertEquals("Jane", dto.getFirstName());
        assertEquals(new Date(1000), dto.getBirthDate());
        assertEquals("F", dto.getGender());
        assertEquals("456 Oak Ave", dto.getAddress());
        assertEquals("111-222-3333", dto.getPhoneNumber());
    }

    @Test
    void updatePatient_shouldIgnoreNulls() {
        PatientDTO patch = new PatientDTO();
        // set only some fields; others remain null and must be preserved
        patch.setLastName("Smith");
        patch.setAddress("789 Pine Rd");

        Patient target = new Patient();
        target.setLastName("Doe");
        target.setFirstName("John");
        target.setBirthDate(new Date(0));
        target.setGender("M");
        target.setAddress("123 Main St");
        target.setPhoneNumber("123-456-7890");
        target.setActive(true);

        mapper.updatePatient(patch, target);

        assertEquals("Smith", target.getLastName()); // updated
        assertEquals("John", target.getFirstName()); // unchanged
        assertEquals(new Date(0), target.getBirthDate()); // unchanged
        assertEquals("M", target.getGender()); // unchanged
        assertEquals("789 Pine Rd", target.getAddress()); // updated
        assertEquals("123-456-7890", target.getPhoneNumber()); // unchanged
        assertTrue(target.getActive());
    }
}
