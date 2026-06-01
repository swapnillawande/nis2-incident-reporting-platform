package com.nisync.savedview.service;

import com.nisync.savedview.dto.CreateSavedViewRequestDto;
import com.nisync.savedview.dto.SavedViewResponseDto;
import com.nisync.savedview.enums.SavedViewType;

import java.util.List;

public interface SavedViewService {

    SavedViewResponseDto createSavedView(CreateSavedViewRequestDto request, String ownerEmail);

    List<SavedViewResponseDto> getSavedViews(SavedViewType viewType, String ownerEmail);

    SavedViewResponseDto deleteSavedView(Long savedViewId, String ownerEmail);
}
