package dev.nikkune.mspatient.service;

import dev.nikkune.mspatient.dto.PatientDTO;
import dev.nikkune.mspatient.mapper.PatientMapper;
import dev.nikkune.mspatient.model.Patient;
import dev.nikkune.mspatient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper mapper;

    @InjectMocks
    private PatientService service;

    private Patient activePatient;
    private Patient inactivePatient;
    private PatientDTO dto;

    @BeforeEach
    void setUp() {
        activePatient = new Patient();
        activePatient.setId(1);
        activePatient.setLastName("Doe");
        activePatient.setFirstName("John");
        activePatient.setBirthDate(new Date(0));
        activePatient.setGender("M");
        activePatient.setAddress("123 Main St");
        activePatient.setPhoneNumber("123-456-7890");
        activePatient.setActive(true);

        inactivePatient = new Patient();
        inactivePatient.setId(2);
        inactivePatient.setLastName("Doe");
        inactivePatient.setFirstName("Jane");
        inactivePatient.setBirthDate(new Date(0));
        inactivePatient.setGender("F");
        inactivePatient.setAddress("456 Oak Ave");
        inactivePatient.setPhoneNumber("111-222-3333");
        inactivePatient.setActive(false);

        dto = new PatientDTO();
        dto.setLastName("Doe");
        dto.setFirstName("John");
        dto.setBirthDate(new Date(0));
        dto.setGender("M");
        dto.setAddress("123 Main St");
        dto.setPhoneNumber("123-456-7890");
    }

    @Test
    void findAll_shouldReturnMappedDTOs() {
        when(patientRepository.findAllByActiveTrue()).thenReturn(Arrays.asList(activePatient));
        when(mapper.toDTO(any(Patient.class))).thenReturn(dto);

        List<PatientDTO> result = service.findAll();

        assertEquals(1, result.size(), "Only active patients should be returned");
        verify(patientRepository, times(1)).findAllByActiveTrue();
        verify(mapper, times(1)).toDTO(any(Patient.class));
    }

    @Test
    void findById_shouldReturnDTO_whenActive() {
        when(patientRepository.findById(1)).thenReturn(Optional.of(activePatient));
        when(mapper.toDTO(activePatient)).thenReturn(dto);

        PatientDTO result = service.findById(1);

        assertNotNull(result);
        verify(patientRepository).findById(1);
        verify(mapper).toDTO(activePatient);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(patientRepository.findById(99)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.findById(99));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void findById_shouldThrow_whenInactive() {
        when(patientRepository.findById(2)).thenReturn(Optional.of(inactivePatient));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.findById(2));
        assertTrue(ex.getMessage().contains("2"));
    }

    @Test
    void findByFirstNameAndLastName_shouldReturnDTO_whenActive() {
        when(patientRepository.findByFirstNameAndLastNameAndActiveTrue("John", "Doe")).thenReturn(activePatient);
        when(mapper.toDTO(activePatient)).thenReturn(dto);

        PatientDTO result = service.findByFirstNameAndLastName("John", "Doe");
        assertNotNull(result);
        verify(patientRepository).findByFirstNameAndLastNameAndActiveTrue("John", "Doe");
        verify(mapper).toDTO(activePatient);
    }

    @Test
    void findByFirstNameAndLastName_shouldThrow_whenNotFound() {
        when(patientRepository.findByFirstNameAndLastNameAndActiveTrue("Ghost", "User")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.findByFirstNameAndLastName("Ghost", "User"));
    }

    @Test
    void registerPatient_shouldSetActiveTrueAndReturnDTO() {
        // mapper to entity
        when(mapper.toPatient(dto)).thenReturn(activePatient);
        // repo save returns entity (assume active gets saved)
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(Patient.class))).thenReturn(dto);

        PatientDTO result = service.registerPatient(dto);

        assertNotNull(result);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        Patient saved = captor.getValue();
        assertTrue(saved.getActive(), "Active flag should be set to true on registration");
    }

    @Test
    void update_shouldPersistChanges_whenActive() {
        when(patientRepository.findByFirstNameAndLastNameAndActiveTrue("John", "Doe")).thenReturn(activePatient);
        // mapper.updatePatient is void; we ensure it is invoked
        doAnswer(inv -> {
            PatientDTO pdto = inv.getArgument(0);
            Patient p = inv.getArgument(1);
            p.setAddress(pdto.getAddress());
            return null;
        }).when(mapper).updatePatient(any(PatientDTO.class), any(Patient.class));

        when(patientRepository.save(activePatient)).thenReturn(activePatient);
        when(mapper.toDTO(activePatient)).thenReturn(dto);

        PatientDTO patch = new PatientDTO();
        patch.setFirstName("John");
        patch.setLastName("Doe");
        patch.setAddress("New Address");

        PatientDTO result = service.update(patch);
        assertNotNull(result);
        verify(patientRepository).findByFirstNameAndLastNameAndActiveTrue("John", "Doe");
        verify(mapper).updatePatient(patch, activePatient);
        verify(patientRepository).save(activePatient);
        verify(mapper).toDTO(activePatient);
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        PatientDTO ghost = new PatientDTO();
        ghost.setFirstName("Ghost");
        ghost.setLastName("User");
        when(patientRepository.findByFirstNameAndLastNameAndActiveTrue("Ghost", "User")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.update(ghost));
    }

    @Test
    void delete_shouldSoftDelete_whenActive() {
        when(patientRepository.findByFirstNameAndLastNameAndActiveTrue("John", "Doe")).thenReturn(activePatient);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete("John", "Doe");

        assertFalse(activePatient.getActive(), "Patient should be marked inactive on delete");
        verify(patientRepository).save(activePatient);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(patientRepository.findByFirstNameAndLastNameAndActiveTrue("Ghost", "User")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.delete("Ghost", "User"));
    }
}
