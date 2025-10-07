package dev.nikkune.msnotes.mapper;

import dev.nikkune.msnotes.dto.NoteDTO;
import dev.nikkune.msnotes.model.Note;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NoteMapperTest {

    private final NoteMapper mapper = Mappers.getMapper(NoteMapper.class);

    @Test
    public void toDto_mapsFields() {
        Note note = new Note();
        note.setFirstName("John");
        note.setLastName("Doe");
        note.setNote("text");
        note.setActive(true);
        note.setCreatedAt(new Date());
        note.setUpdatedAt(new Date());

        NoteDTO dto = mapper.toDto(note);
        assertEquals("text", dto.getNote());
        assertNotNull(dto.getCreatedAt());
    }

    @Test
    public void toDtoList_mapsAll() {
        Note n1 = new Note(); n1.setFirstName("A"); n1.setLastName("B"); n1.setNote("1"); n1.setCreatedAt(new Date());
        Note n2 = new Note(); n2.setFirstName("C"); n2.setLastName("D"); n2.setNote("2"); n2.setCreatedAt(new Date());
        List<NoteDTO> dtos = mapper.toDtoList(Arrays.asList(n1, n2));
        assertEquals(2, dtos.size());
        assertEquals("1", dtos.get(0).getNote());
        assertEquals("2", dtos.get(1).getNote());
    }
}
