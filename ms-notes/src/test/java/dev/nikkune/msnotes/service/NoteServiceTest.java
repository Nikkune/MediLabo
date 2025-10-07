package dev.nikkune.msnotes.service;

import dev.nikkune.msnotes.client.PatientClient;
import dev.nikkune.msnotes.exception.NoteNotFoundException;
import dev.nikkune.msnotes.exception.PatientNotFoundException;
import dev.nikkune.msnotes.model.Note;
import dev.nikkune.msnotes.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoteServiceTest {

    private NoteRepository noteRepository;
    private PatientClient patientClient;
    private NoteService noteService;

    @BeforeEach
    public void setUp() {
        noteRepository = Mockito.mock(NoteRepository.class);
        patientClient = Mockito.mock(PatientClient.class);
        noteService = new NoteService(noteRepository, patientClient);
    }

    @Test
    public void listNotes_returnsNotes_whenPatientExists() {
        when(patientClient.exists("John", "Doe")).thenReturn(true);
        Note n1 = new Note(); n1.setFirstName("John"); n1.setLastName("Doe"); n1.setNote("a"); n1.setCreatedAt(new Date());
        when(noteRepository.findByFirstNameAndLastNameAndActiveTrueOrderByUpdatedAtDesc("John","Doe"))
                .thenReturn(Arrays.asList(n1));

        List<Note> result = noteService.list("John","Doe");

        assertEquals(1, result.size());
        assertEquals("a", result.get(0).getNote());
    }

    @Test
    public void listNotes_throws404_whenPatientMissing() {
        when(patientClient.exists("Jane", "Smith")).thenReturn(false);
        assertThrows(PatientNotFoundException.class, () -> noteService.list("Jane","Smith"));
    }

    @Test
    public void addNote_persistsWithTimestamps_whenPatientExists() {
        when(patientClient.exists("John", "Doe")).thenReturn(true);
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note created = noteService.add("John","Doe","Observation text");

        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertEquals(Boolean.TRUE, created.getActive());
        assertEquals("John", created.getFirstName());
        assertEquals("Doe", created.getLastName());
        assertEquals("Observation text", created.getNote());

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note saved = captor.getValue();
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    public void addNote_throws404_whenPatientMissing() {
        when(patientClient.exists("Ghost", "User")).thenReturn(false);
        assertThrows(PatientNotFoundException.class, () -> noteService.add("Ghost","User","text"));
    }

    @Test
    public void get_returnsNote_whenExists() {
        Note n = new Note(); n.setNote("abc"); n.setActive(true);
        when(noteRepository.findById("id1")).thenReturn(Optional.of(n));
        Note res = noteService.get("id1");
        assertEquals("abc", res.getNote());
    }

    @Test
    public void get_throws404_whenMissing() {
        when(noteRepository.findById("id1")).thenReturn(Optional.empty());
        assertThrows(NoteNotFoundException.class, () -> noteService.get("id1"));
    }

    @Test
    public void update_updatesNoteText_andTimestamp() {
        Note existing = new Note(); existing.setNote("old"); existing.setUpdatedAt(new Date(0)); existing.setActive(true);
        when(noteRepository.findById("id1")).thenReturn(Optional.of(existing));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note res = noteService.update("id1", "new");
        assertEquals("new", res.getNote());
        assertTrue(res.getUpdatedAt().getTime() >= new Date().getTime() - 2000); // updated recently
    }

    @Test
    public void delete_throws404_whenMissing() {
        when(noteRepository.findById("id1")).thenReturn(Optional.empty());
        assertThrows(NoteNotFoundException.class, () -> noteService.delete("id1"));
    }

    @Test
    public void delete_callsRepository_whenExists() {
        Note existing = new Note(); existing.setActive(true);
        when(noteRepository.findById("id1")).thenReturn(Optional.of(existing));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        noteService.delete("id1");

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note saved = captor.getValue();
        assertEquals(Boolean.FALSE, saved.getActive());
    }

    @Test
    public void get_throws404_whenInactive() {
        Note n = new Note(); n.setActive(false);
        when(noteRepository.findById("id1")).thenReturn(Optional.of(n));
        assertThrows(NoteNotFoundException.class, () -> noteService.get("id1"));
    }

    @Test
    public void update_throws404_whenInactive() {
        Note existing = new Note(); existing.setActive(false);
        when(noteRepository.findById("id1")).thenReturn(Optional.of(existing));
        assertThrows(NoteNotFoundException.class, () -> noteService.update("id1", "x"));
    }

    @Test
    public void delete_throws404_whenInactive() {
        Note existing = new Note(); existing.setActive(false);
        when(noteRepository.findById("id1")).thenReturn(Optional.of(existing));
        assertThrows(NoteNotFoundException.class, () -> noteService.delete("id1"));
    }
}
