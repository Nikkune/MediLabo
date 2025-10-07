package dev.nikkune.msnotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nikkune.msnotes.model.Note;
import dev.nikkune.msnotes.service.INoteService;
import dev.nikkune.msnotes.mapper.NoteMapper;
import dev.nikkune.msnotes.dto.NoteDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = NotesController.class)
public class NotesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private INoteService noteService;

    @MockBean
    private NoteMapper noteMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getNotes_requiresAuth() throws Exception {
        mockMvc.perform(get("/").param("firstName","John").param("lastName","Doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getNotes_returnsList_whenAuthenticated() throws Exception {
        Note n = new Note(); n.setFirstName("John"); n.setLastName("Doe"); n.setNote("obs"); n.setCreatedAt(new Date());
        when(noteService.list("John","Doe")).thenReturn(Arrays.asList(n));
        NoteDTO dtoListItem = new NoteDTO(); dtoListItem.setNote("obs");
        when(noteMapper.toDtoList(any())).thenReturn(Arrays.asList(dtoListItem));

        mockMvc.perform(get("/").param("firstName","John").param("lastName","Doe").with(httpBasic("medilabo","medilabo123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].note", is("obs")));
    }

    @Test
    public void postNotes_createsNote_whenAuthenticated() throws Exception {
        Note created = new Note(); created.setFirstName("John"); created.setLastName("Doe"); created.setNote("text"); created.setCreatedAt(new Date());
        when(noteService.add(eq("John"), eq("Doe"), eq("text"))).thenReturn(created);
        NoteDTO createdDto = new NoteDTO(); createdDto.setNote("text");
        when(noteMapper.toDto(created)).thenReturn(createdDto);

        String payload = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"note\":\"text\"}";

        mockMvc.perform(post("/").with(httpBasic("medilabo","medilabo123")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("text")));
    }

    @Test
    public void getById_returnsNote_whenAuthenticated() throws Exception {
        Note n = new Note(); n.setFirstName("John"); n.setLastName("Doe"); n.setNote("one"); n.setCreatedAt(new Date());
        when(noteService.get("abc123")).thenReturn(n);
        NoteDTO dto = new NoteDTO(); dto.setNote("one");
        when(noteMapper.toDto(n)).thenReturn(dto);

        mockMvc.perform(get("/byId").param("id","abc123").with(httpBasic("medilabo","medilabo123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("one")));
    }

    @Test
    public void put_updatesNote_whenAuthenticated() throws Exception {
        Note updated = new Note(); updated.setFirstName("John"); updated.setLastName("Doe"); updated.setNote("updated"); updated.setCreatedAt(new Date());
        when(noteService.update(eq("abc123"), eq("updated"))).thenReturn(updated);
        NoteDTO updatedDto = new NoteDTO(); updatedDto.setNote("updated");
        when(noteMapper.toDto(updated)).thenReturn(updatedDto);

        String payload = "{\"note\":\"updated\"}";

        mockMvc.perform(put("/").param("id","abc123").with(httpBasic("medilabo","medilabo123")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("updated")));
    }

    @Test
    public void delete_deletesNote_whenAuthenticated() throws Exception {
        mockMvc.perform(delete("/").param("id","abc123").with(httpBasic("medilabo","medilabo123")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    public void getNotes_returns400_whenFirstNameBlank() throws Exception {
        try {
            mockMvc.perform(get("/")
                            .param("firstName"," ")
                            .param("lastName","Doe")
                            .with(httpBasic("medilabo","medilabo123")))
                    .andReturn();
            // If no exception is thrown, the test should fail
            org.junit.jupiter.api.Assertions.fail("Expected validation exception to be thrown");
        } catch (jakarta.servlet.ServletException ex) {
            // Expected: validation failure leads to ServletException wrapping ConstraintViolationException
            org.junit.jupiter.api.Assertions.assertTrue(ex.getCause() instanceof jakarta.validation.ConstraintViolationException);
        }
    }

    @Test
    public void create_returns400_whenPayloadInvalid() throws Exception {
        String invalid = "{\"firstName\":\"\",\"lastName\":\"Doe\",\"note\":\"\"}";
        mockMvc.perform(post("/")
                        .with(httpBasic("medilabo","medilabo123"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update_returns400_whenNoteBlank() throws Exception {
        String invalid = "{\"note\":\"\"}";
        mockMvc.perform(put("/")
                        .param("id","abc123")
                        .with(httpBasic("medilabo","medilabo123"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }
}
