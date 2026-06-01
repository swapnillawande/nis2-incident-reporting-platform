package com.nisync.savedview.dto;

import com.nisync.savedview.enums.SavedViewType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSavedViewRequestDto {

    @NotNull
    private SavedViewType viewType;

    @NotBlank
    @Size(max = 80)
    private String name;

    @NotBlank
    @Size(max = 4000)
    private String filterJson;

    public SavedViewType getViewType() {
        return viewType;
    }

    public void setViewType(SavedViewType viewType) {
        this.viewType = viewType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilterJson() {
        return filterJson;
    }

    public void setFilterJson(String filterJson) {
        this.filterJson = filterJson;
    }
}
