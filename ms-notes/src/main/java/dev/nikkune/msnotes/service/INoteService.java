package dev.nikkune.msnotes.service;

import dev.nikkune.msnotes.model.Note;

import java.util.List;

public interface INoteService {
    List<Note> list(String firstName, String lastName);
    Note add(String firstName, String lastName, String noteText);
    Note get(String id);
    Note update(String id, String noteText);
    void delete(String id);
}
