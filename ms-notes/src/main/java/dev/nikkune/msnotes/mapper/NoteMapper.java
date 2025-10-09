package dev.nikkune.msnotes.mapper;

import dev.nikkune.msnotes.dto.NoteDTO;
import dev.nikkune.msnotes.model.Note;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    NoteDTO toDto(Note note);

    List<NoteDTO> toDtoList(List<Note> notes);
}
