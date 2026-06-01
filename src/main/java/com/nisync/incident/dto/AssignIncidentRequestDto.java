package com.nisync.incident.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AssignIncidentRequestDto {

    @NotBlank(message = "Assigned user email is required")
    @Email(message = "Assigned user email must be valid")
    @Size(max = 150, message = "Assigned user email must be at most 150 characters")
    private String assignedToEmail;

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }
}
