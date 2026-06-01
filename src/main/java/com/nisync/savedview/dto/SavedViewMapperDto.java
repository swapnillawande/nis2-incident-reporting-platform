package com.nisync.savedview.dto;

import com.nisync.savedview.entity.SavedView;

public final class SavedViewMapperDto {

    private SavedViewMapperDto() {
    }

    public static SavedViewResponseDto toResponse(SavedView savedView) {
        SavedViewResponseDto response = new SavedViewResponseDto();
        response.setId(savedView.getId());
        response.setViewType(savedView.getViewType());
        response.setName(savedView.getName());
        response.setFilterJson(savedView.getFilterJson());
        response.setCreatedAt(savedView.getCreatedAt());
        response.setUpdatedAt(savedView.getUpdatedAt());
        return response;
    }
}
