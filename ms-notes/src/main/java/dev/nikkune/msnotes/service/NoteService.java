package dev.nikkune.msnotes.service;

import dev.nikkune.msnotes.client.PatientClient;
import dev.nikkune.msnotes.exception.NoteNotFoundException;
import dev.nikkune.msnotes.exception.PatientNotFoundException;
import dev.nikkune.msnotes.model.Note;
import dev.nikkune.msnotes.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NoteService implements INoteService {
    private final NoteRepository noteRepository;
    private final PatientClient patientClient;

    public NoteService(NoteRepository noteRepository, PatientClient patientClient) {
        this.noteRepository = noteRepository;
        this.patientClient = patientClient;
    }

    public List<Note> list(String firstName, String lastName) {
        ensurePatientExists(firstName, lastName);
        return noteRepository.findByFirstNameAndLastNameOrderByCreatedAtDesc(firstName, lastName);
    }

    public Note add(String firstName, String lastName, String noteText) {
        ensurePatientExists(firstName, lastName);
        Date now = new Date();
        Note note = new Note();
        note.setFirstName(firstName);
        note.setLastName(lastName);
        note.setNote(noteText);
        note.setActive(true);
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        return noteRepository.save(note);
    }

    public Note get(String id) {
        return noteRepository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
    }

    public Note update(String id, String noteText) {
        Note existing = get(id);
        existing.setNote(noteText);
        existing.setUpdatedAt(new Date());
        return noteRepository.save(existing);
    }

    public void delete(String id) {
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException(id);
        }
        noteRepository.deleteById(id);
    }

    private void ensurePatientExists(String firstName, String lastName) {
        if (!patientClient.exists(firstName, lastName)) {
            throw new PatientNotFoundException(firstName, lastName);
        }
    }
}
