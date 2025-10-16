package dev.nikkune.msnotes.service;

import dev.nikkune.msnotes.exception.PatientNotFoundException;
import dev.nikkune.msnotes.model.Note;

import java.util.List;

/**
 * Service interface for managing notes associated with patients.
 * Provides operations to create, retrieve, update, and delete notes,
 * as well as to list notes for specific patients.
 */
public interface INoteService {
    /**
     * Retrieves a list of active notes associated with a patient,
     * identified by their first and last name. The results are sorted
     * in descending order by the last update timestamp.
     *
     * @param firstName the first name of the patient
     * @param lastName  the last name of the patient
     * @return a list of active notes for the specified patient
     * @throws PatientNotFoundException if the patient does not exist
     */
    List<Note> list(String firstName, String lastName);

    /**
     * Adds a new active note for a patient, ensuring the patient exists before creating the note.
     * The note is saved with the current timestamp for creation and update time.
     *
     * @param firstName the first name of the patient associated with the note
     * @param lastName  the last name of the patient associated with the note
     * @param noteText  the content of the note to be added
     * @return the saved {@code Note} object
     */
    Note add(String firstName, String lastName, String noteText);

    /**
     * Retrieves a note by its unique identifier if it exists and is active.
     *
     * @param id the unique identifier of the note to retrieve
     * @return the {@code Note} object corresponding to the specified identifier
     * @throws NoteNotFoundException if the note with the provided ID does not exist or is inactive
     */
    Note get(String id);

    /**
     * Updates the text of an existing note identified by its unique ID and updates the note's timestamp.
     * The note must be active; otherwise, an exception is thrown.
     *
     * @param id       the unique identifier of the note to be updated
     * @param noteText the new content to replace the existing note text
     * @return the updated {@code Note} object after changes are saved to the repository
     * @throws NoteNotFoundException if the note with the provided ID does not exist or is inactive
     */
    Note update(String id, String noteText);

    /**
     * Marks a note as inactive, effectively treating it as deleted. The note
     * must exist and be active; otherwise, a {@code NoteNotFoundException} is thrown.
     *
     * @param id the unique identifier of the note to be deleted
     * @throws NoteNotFoundException if the note with the specified id does not exist or is already inactive
     */
    void delete(String id);
}
