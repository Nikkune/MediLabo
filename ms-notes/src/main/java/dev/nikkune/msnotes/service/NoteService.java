package dev.nikkune.msnotes.service;

import dev.nikkune.msnotes.client.PatientClient;
import dev.nikkune.msnotes.exception.NoteNotFoundException;
import dev.nikkune.msnotes.exception.PatientNotFoundException;
import dev.nikkune.msnotes.model.Note;
import dev.nikkune.msnotes.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Service class for managing notes associated with patients. This class provides
 * functionality to add, retrieve, update, delete, and list active notes, while
 * ensuring the associated patient exists.
 */
@Service
public class NoteService implements INoteService {
    private final NoteRepository noteRepository;
    private final PatientClient patientClient;

    /**
     * Constructs a new instance of NoteService.
     *
     * @param noteRepository the repository used for managing note persistence
     * @param patientClient the client used to interact with the patient microservice
     */
    public NoteService(NoteRepository noteRepository, PatientClient patientClient) {
        this.noteRepository = noteRepository;
        this.patientClient = patientClient;
    }

    /**
     * Retrieves a list of active notes associated with a patient, filtered by their first and last name,
     * and sorted in descending order based on creation time.
     *
     * @param firstName the first name of the patient
     * @param lastName the last name of the patient
     * @return a list of active notes for the specified patient
     * @throws PatientNotFoundException if the patient does not exist
     */
    public List<Note> list(String firstName, String lastName) {
        ensurePatientExists(firstName, lastName);
        return noteRepository.findByFirstNameAndLastNameAndActiveTrueOrderByUpdatedAtDesc(firstName, lastName);
    }

    /**
     * Adds a new active note for a patient, ensuring the patient exists before creating the note.
     * The note is saved with the current timestamp for creation and update time.
     *
     * @param firstName the first name of the patient associated with the note
     * @param lastName the last name of the patient associated with the note
     * @param noteText the content of the note to be added
     * @return the saved {@code Note} object
     */
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

    /**
     * Retrieves a note by its identifier if it exists and is active.
     *
     * @param id the unique identifier of the note to retrieve
     * @return the requested {@code Note} object
     * @throws NoteNotFoundException if the note with the specified id is not found or is inactive
     */
    public Note get(String id) {
        Note note = noteRepository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
        if (!note.getActive()) {
            throw new NoteNotFoundException(id);
        }
        return note;
    }

    /**
     * Updates the text of an existing note identified by its ID and sets the updated timestamp.
     * The note must be active; otherwise, a {@link NoteNotFoundException} is thrown.
     *
     * @param id the unique identifier of the note to be updated
     * @param noteText the new text content for the note
     * @return the updated {@link Note} after saving changes to the repository
     * @throws NoteNotFoundException if the note with the provided ID does not exist or is inactive
     */
    public Note update(String id, String noteText) {
        Note existing = get(id);
        if (!existing.getActive()) {
            throw new NoteNotFoundException(id);
        }
        existing.setNote(noteText);
        existing.setUpdatedAt(new Date());
        return noteRepository.save(existing);
    }

    /**
     * Marks a note as inactive. If the note does not exist or is already inactive,
     * throws a NoteNotFoundException.
     *
     * @param id the unique identifier of the note to be deleted
     * @throws NoteNotFoundException if the note does not exist or is already inactive
     */
    public void delete(String id) {
        Note existing = get(id);
        if (!existing.getActive()) {
            throw new NoteNotFoundException(id);
        }
        existing.setActive(false);
        existing.setUpdatedAt(new Date());
        noteRepository.save(existing);
    }

    /**
     * Ensures that a patient exists by verifying their existence via the patient client.
     * If the patient does not exist, a {@link PatientNotFoundException} is thrown.
     *
     * @param firstName the first name of the patient to verify
     * @param lastName  the last name of the patient to verify
     * @throws PatientNotFoundException if the patient does not exist
     */
    private void ensurePatientExists(String firstName, String lastName) {
        if (!patientClient.exists(firstName, lastName)) {
            throw new PatientNotFoundException(firstName, lastName);
        }
    }
}
