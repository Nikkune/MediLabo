package dev.nikkune.msnotes.controller;

import dev.nikkune.msnotes.dto.CreateNoteDTO;
import dev.nikkune.msnotes.dto.NoteDTO;
import dev.nikkune.msnotes.dto.UpdateNoteDTO;
import dev.nikkune.msnotes.mapper.NoteMapper;
import dev.nikkune.msnotes.model.Note;
import dev.nikkune.msnotes.service.INoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A REST controller for managing note-related operations.
 * Provides endpoints to retrieve, create, update, and delete notes.
 */
@RestController
@RequestMapping
@Validated
public class NotesController {
    private final INoteService noteService;
    private final NoteMapper mapper;

    /**
     * Constructs a NotesController instance with the required dependencies.
     *
     * @param noteService the service responsible for note management operations
     * @param mapper      the mapper to convert between Note and NoteDTO objects
     */
    public NotesController(INoteService noteService, NoteMapper mapper) {
        this.noteService = noteService;
        this.mapper = mapper;
    }

    /**
     * Retrieves a list of notes associated with the specified first name and last name.
     *
     * @param firstName the first name of the user to filter notes, must not be blank
     * @param lastName  the last name of the user to filter notes, must not be blank
     * @return a list of NoteDTO objects containing the filtered notes
     */
    @GetMapping
    public List<NoteDTO> getNotes(@RequestParam @NotBlank String firstName,
                                  @RequestParam @NotBlank String lastName) {
        List<Note> notes = noteService.list(firstName, lastName);
        return mapper.toDtoList(notes);
    }

    /**
     * Retrieves a note by its unique identifier.
     *
     * @param id the unique identifier of the note to retrieve
     * @return the data transfer object representing the note with the specified id
     */
    @GetMapping(value = "/byId")
    public NoteDTO getOne(@RequestParam String id) {
        Note note = noteService.get(id);
        return mapper.toDto(note);
    }

    /**
     * Creates a new note using the provided request data.
     *
     * @param request the request body containing the required details to create the note,
     *                including firstName, lastName, and note content.
     * @return the created note as a data transfer object (DTO).
     */
    @PostMapping
    public NoteDTO create(@RequestBody @Valid CreateNoteDTO request) {
        Note created = noteService.add(request.getFirstName(), request.getLastName(), request.getNote());
        return mapper.toDto(created);
    }

    /**
     * Updates the content of an existing note based on the provided ID and request data.
     *
     * @param id      the unique identifier of the note to be updated
     * @param request the data used to update the note, including the updated note text
     * @return a representation of the updated note in the form of a NoteDTO
     */
    @PutMapping
    public NoteDTO update(@RequestParam String id, @RequestBody @Valid UpdateNoteDTO request) {
        Note updated = noteService.update(id, request.getNote());
        return mapper.toDto(updated);
    }

    /**
     * Deletes a note based on the provided unique identifier.
     *
     * @param id the unique identifier of the note to be deleted
     * @return a ResponseEntity with no content to indicate successful deletion
     */
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String id) {
        noteService.delete(id);
        return ResponseEntity.ok().build();
    }
}
