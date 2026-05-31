package com.nisync.incident.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateIncidentNoteRequestDto {

    @NotBlank(message = "Note is required")
    @Size(min = 2, max = 1500, message = "Note must be between 2 and 1500 characters")
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
