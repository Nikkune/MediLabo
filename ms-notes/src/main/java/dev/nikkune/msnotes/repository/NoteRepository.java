package dev.nikkune.msnotes.repository;

import dev.nikkune.msnotes.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository interface for managing {@link Note} entities in a MongoDB database.
 * Extends {@link MongoRepository} to leverage Spring Data MongoDB's repository functionality.
 * Provides custom query methods specific to {@link Note} entities.
 */
public interface NoteRepository extends MongoRepository<Note, String> {
    /**
     * Retrieves a list of active {@code Note} objects filtered by the specified first name and last name.
     * The results are sorted in descending order based on the {@code updatedAt} timestamp.
     *
     * @param firstName the first name of the patient to filter the notes by
     * @param lastName  the last name of the patient to filter the notes by
     * @return a list of active {@code Note} objects matching the specified first name and last name,
     * sorted by the most recently updated first
     */
    List<Note> findByFirstNameAndLastNameAndActiveTrueOrderByUpdatedAtDesc(String firstName, String lastName);
}
