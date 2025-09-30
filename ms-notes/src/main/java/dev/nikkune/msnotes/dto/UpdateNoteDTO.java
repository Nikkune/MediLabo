package dev.nikkune.msnotes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateNoteDTO {
    @NotBlank
    private String note;
}
