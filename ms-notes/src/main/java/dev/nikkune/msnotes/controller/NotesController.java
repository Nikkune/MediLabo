package dev.nikkune.msnotes.controller;

import dev.nikkune.msnotes.dto.CreateNoteDTO;
import dev.nikkune.msnotes.dto.NoteDTO;
import dev.nikkune.msnotes.dto.UpdateNoteDTO;
import dev.nikkune.msnotes.mapper.NoteMapper;
import dev.nikkune.msnotes.model.Note;
import dev.nikkune.msnotes.service.INoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@Validated
public class NotesController {

    private final INoteService noteService;

    public NotesController(INoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<NoteDTO> getNotes(@RequestParam @NotBlank String firstName,
                               @RequestParam @NotBlank String lastName) {
        List<Note> notes = noteService.list(firstName, lastName);
        return NoteMapper.INSTANCE.toDtoList(notes);
    }

    @GetMapping(value = "/byId")
    public NoteDTO getOne(@RequestParam String id) {
        Note note = noteService.get(id);
        return NoteMapper.INSTANCE.toDto(note);
    }

    @PostMapping
    public NoteDTO create(@RequestBody @Valid CreateNoteDTO request) {
        Note created = noteService.add(request.getFirstName(), request.getLastName(), request.getNote());
        return NoteMapper.INSTANCE.toDto(created);
    }

    @PutMapping
    public NoteDTO update(@RequestParam String id, @RequestBody @Valid UpdateNoteDTO request) {
        Note updated = noteService.update(id, request.getNote());
        return NoteMapper.INSTANCE.toDto(updated);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String id) {
        noteService.delete(id);
        return ResponseEntity.ok().build();
    }
}
