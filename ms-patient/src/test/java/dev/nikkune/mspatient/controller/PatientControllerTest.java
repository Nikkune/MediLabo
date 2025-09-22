package dev.nikkune.mspatient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nikkune.mspatient.dto.PatientDTO;
import dev.nikkune.mspatient.exception.GlobalExceptionHandler;
import dev.nikkune.mspatient.service.IPatientService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PatientController.class)
@Import({GlobalExceptionHandler.class, PatientControllerTest.MockConfig.class})
@WithMockUser
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IPatientService patientService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        IPatientService patientService() {
            return Mockito.mock(IPatientService.class);
        }
    }

    private PatientDTO sampleDto() {
        PatientDTO dto = new PatientDTO();
        dto.setLastName("Doe");
        dto.setFirstName("John");
        dto.setBirthDate(new Date(0));
        dto.setGender("M");
        dto.setAddress("123 Main St");
        dto.setPhoneNumber("123-456-7890");
        return dto;
    }

    @Test
    void getAllPatients_returnsList() throws Exception {
        List<PatientDTO> list = Arrays.asList(sampleDto(), sampleDto());
        given(patientService.findAll()).willReturn(list);

        mockMvc.perform(get("/patient/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lastName", is("Doe")));
    }

    @Test
    void getPatientById_returnsPatient() throws Exception {
        given(patientService.findById(1)).willReturn(sampleDto());

        mockMvc.perform(get("/patient/byId").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")));
    }

    @Test
    void getPatientById_returns404_whenServiceThrows() throws Exception {
        given(patientService.findById(99)).willThrow(new RuntimeException("Patient with ID 99 does not exist"));

        mockMvc.perform(get("/patient/byId").param("id", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Not found")))
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void getByFirstNameAndLastName_returnsPatient() throws Exception {
        given(patientService.findByFirstNameAndLastName("John", "Doe")).willReturn(sampleDto());

        mockMvc.perform(get("/patient").param("firstName", "John").param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    void createPatient_validPayload_returnsCreated() throws Exception {
        PatientDTO payload = sampleDto();
        given(patientService.registerPatient(any(PatientDTO.class))).willReturn(payload);

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber", is("123-456-7890")));
    }

    @Test
    void createPatient_invalidPayload_returns400() throws Exception {
        PatientDTO payload = new PatientDTO(); // missing required fields for Create group

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors", aMapWithSize(greaterThan(0))));
    }

    @Test
    void updatePatient_returnsUpdated() throws Exception {
        PatientDTO patch = new PatientDTO();
        patch.setAddress("New Address"); // Update group should allow partials
        given(patientService.update(any(PatientDTO.class), eq(1))).willReturn(sampleDto());

        mockMvc.perform(put("/patient")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")));
    }

    @Test
    void deletePatient_returns200() throws Exception {
        mockMvc.perform(delete("/patient").param("id", "1"))
                .andExpect(status().isOk());
    }
}
