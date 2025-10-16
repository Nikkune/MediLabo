package dev.nikkune.msnotes.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NoteDTO {
    private String id;
    private String note;
    private Date createdAt;
    private Date updatedAt;
}
