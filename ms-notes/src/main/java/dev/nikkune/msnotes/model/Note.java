package dev.nikkune.msnotes.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "notes")
public class Note {
    @Id
    private String id;

    private String firstName;
    private String lastName;
    private Boolean active;
    private String note;
    private Date createdAt;
    private Date updatedAt;
}
