package com.nisync.savedview.service.impl;

import com.nisync.audit.service.AuditLogService;
import com.nisync.common.exception.DuplicateResourceException;
import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.savedview.dto.CreateSavedViewRequestDto;
import com.nisync.savedview.dto.SavedViewMapperDto;
import com.nisync.savedview.dto.SavedViewResponseDto;
import com.nisync.savedview.entity.SavedView;
import com.nisync.savedview.enums.SavedViewType;
import com.nisync.savedview.repository.SavedViewRepository;
import com.nisync.savedview.service.SavedViewService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedViewServiceImpl implements SavedViewService {

    @Autowired
    private SavedViewRepository savedViewRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public SavedViewResponseDto createSavedView(CreateSavedViewRequestDto request, String ownerEmail) {
        String normalizedName = request.getName().trim();

        if (savedViewRepository.existsByOwnerEmailAndViewTypeAndNameIgnoreCase(
                ownerEmail,
                request.getViewType(),
                normalizedName
        )) {
            throw new DuplicateResourceException("Saved view already exists: " + normalizedName);
        }

        SavedView savedView = new SavedView();
        savedView.setOwnerEmail(ownerEmail);
        savedView.setViewType(request.getViewType());
        savedView.setName(normalizedName);
        savedView.setFilterJson(request.getFilterJson().trim());

        SavedView saved = savedViewRepository.save(savedView);

        auditLogService.record(
                "SAVED_VIEW_CREATED",
                "SAVED_VIEW",
                saved.getId(),
                ownerEmail,
                "Saved view created: " + saved.getName()
        );

        return SavedViewMapperDto.toResponse(saved);
    }

    @Override
    public List<SavedViewResponseDto> getSavedViews(SavedViewType viewType, String ownerEmail) {
        return savedViewRepository.findByOwnerEmailAndViewTypeOrderByUpdatedAtDesc(ownerEmail, viewType)
                .stream()
                .map(SavedViewMapperDto::toResponse)
                .toList();
    }

    @Override
    public SavedViewResponseDto deleteSavedView(Long savedViewId, String ownerEmail) {
        SavedView savedView = savedViewRepository.findByIdAndOwnerEmail(savedViewId, ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Saved view not found with id: " + savedViewId));
        SavedViewResponseDto response = SavedViewMapperDto.toResponse(savedView);

        savedViewRepository.delete(savedView);
        auditLogService.record(
                "SAVED_VIEW_DELETED",
                "SAVED_VIEW",
                savedViewId,
                ownerEmail,
                "Saved view deleted: " + savedView.getName()
        );

        return response;
    }
}
