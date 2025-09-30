package dev.nikkune.msnotes.model;

import lombok.Data;

@Data
public class Note {
    private String lastName;
    private String firstName;
    private ? note;
    private Boolean active;
}
