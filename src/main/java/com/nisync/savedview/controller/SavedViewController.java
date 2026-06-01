package com.nisync.savedview.controller;

import com.nisync.savedview.dto.CreateSavedViewRequestDto;
import com.nisync.savedview.dto.SavedViewResponseDto;
import com.nisync.savedview.enums.SavedViewType;
import com.nisync.savedview.service.SavedViewService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("saved-views")
public class SavedViewController {

    @Autowired
    private SavedViewService savedViewService;

    @PostMapping
    public SavedViewResponseDto createSavedView(
            @Valid @RequestBody CreateSavedViewRequestDto request,
            Authentication authentication) {
        return savedViewService.createSavedView(request, authentication.getName());
    }

    @GetMapping
    public List<SavedViewResponseDto> getSavedViews(
            @RequestParam(name = "viewType") SavedViewType viewType,
            Authentication authentication) {
        return savedViewService.getSavedViews(viewType, authentication.getName());
    }

    @DeleteMapping("/{savedViewId}")
    public SavedViewResponseDto deleteSavedView(
            @PathVariable("savedViewId") Long savedViewId,
            Authentication authentication) {
        return savedViewService.deleteSavedView(savedViewId, authentication.getName());
    }
}
