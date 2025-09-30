package dev.nikkune.msnotes.repository;

import dev.nikkune.msnotes.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByFirstNameAndLastNameOrderByCreatedAtDesc(String firstName, String lastName);
}
