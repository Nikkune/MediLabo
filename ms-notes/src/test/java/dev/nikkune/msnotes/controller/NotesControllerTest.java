package dev.nikkune.msnotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nikkune.msnotes.model.Note;
import dev.nikkune.msnotes.service.INoteService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = NotesController.class)
public class NotesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private INoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getNotes_requiresAuth() throws Exception {
        mockMvc.perform(get("/notes").param("firstName","John").param("lastName","Doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getNotes_returnsList_whenAuthenticated() throws Exception {
        Note n = new Note(); n.setFirstName("John"); n.setLastName("Doe"); n.setNote("obs"); n.setCreatedAt(new Date());
        when(noteService.list("John","Doe")).thenReturn(Arrays.asList(n));

        mockMvc.perform(get("/notes").param("firstName","John").param("lastName","Doe").with(httpBasic("medilabo","medilabo123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].note", is("obs")));
    }

    @Test
    public void postNotes_createsNote_whenAuthenticated() throws Exception {
        Note created = new Note(); created.setFirstName("John"); created.setLastName("Doe"); created.setNote("text"); created.setCreatedAt(new Date());
        when(noteService.add(eq("John"), eq("Doe"), eq("text"))).thenReturn(created);

        String payload = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"note\":\"text\"}";

        mockMvc.perform(post("/notes").with(httpBasic("medilabo","medilabo123")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("text")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    public void getById_returnsNote_whenAuthenticated() throws Exception {
        Note n = new Note(); n.setFirstName("John"); n.setLastName("Doe"); n.setNote("one"); n.setCreatedAt(new Date());
        when(noteService.get("abc123")).thenReturn(n);

        mockMvc.perform(get("/notes/abc123").with(httpBasic("medilabo","medilabo123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("one")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    public void put_updatesNote_whenAuthenticated() throws Exception {
        Note updated = new Note(); updated.setFirstName("John"); updated.setLastName("Doe"); updated.setNote("updated"); updated.setCreatedAt(new Date());
        when(noteService.update(eq("abc123"), eq("updated"))).thenReturn(updated);

        String payload = "{\"note\":\"updated\"}";

        mockMvc.perform(put("/notes/abc123").with(httpBasic("medilabo","medilabo123")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note", is("updated")));
    }

    @Test
    public void delete_deletesNote_whenAuthenticated() throws Exception {
        mockMvc.perform(delete("/notes/abc123").with(httpBasic("medilabo","medilabo123")).with(csrf()))
                .andExpect(status().isOk());
    }
}
