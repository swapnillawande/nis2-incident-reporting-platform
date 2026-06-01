package com.nisync.savedview.service;

import com.nisync.audit.service.AuditLogService;
import com.nisync.common.exception.DuplicateResourceException;
import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.savedview.dto.CreateSavedViewRequestDto;
import com.nisync.savedview.dto.SavedViewResponseDto;
import com.nisync.savedview.entity.SavedView;
import com.nisync.savedview.enums.SavedViewType;
import com.nisync.savedview.repository.SavedViewRepository;
import com.nisync.savedview.service.impl.SavedViewServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SavedViewServiceImplTest {

    private SavedViewRepository savedViewRepository;
    private AuditLogService auditLogService;
    private SavedViewServiceImpl savedViewService;

    @BeforeEach
    void setUp() {
        savedViewRepository = mock(SavedViewRepository.class);
        auditLogService = mock(AuditLogService.class);
        savedViewService = new SavedViewServiceImpl();

        ReflectionTestUtils.setField(savedViewService, "savedViewRepository", savedViewRepository);
        ReflectionTestUtils.setField(savedViewService, "auditLogService", auditLogService);
    }

    @Test
    void shouldCreateSavedViewSuccessfully() {
        CreateSavedViewRequestDto request = buildRequest();

        when(savedViewRepository.existsByOwnerEmailAndViewTypeAndNameIgnoreCase(
                "admin@nis2.com",
                SavedViewType.INCIDENTS,
                "Open incidents"
        )).thenReturn(false);
        when(savedViewRepository.save(any(SavedView.class))).thenAnswer(invocation -> {
            SavedView savedView = invocation.getArgument(0);
            savedView.setId(1L);
            savedView.setCreatedAt(LocalDateTime.now());
            savedView.setUpdatedAt(LocalDateTime.now());
            return savedView;
        });

        SavedViewResponseDto response = savedViewService.createSavedView(request, "admin@nis2.com");

        assertEquals(1L, response.getId());
        assertEquals(SavedViewType.INCIDENTS, response.getViewType());
        assertEquals("Open incidents", response.getName());
        assertEquals("{\"status\":\"OPEN\"}", response.getFilterJson());
        verify(auditLogService).record(
                eq("SAVED_VIEW_CREATED"),
                eq("SAVED_VIEW"),
                eq(1L),
                eq("admin@nis2.com"),
                eq("Saved view created: Open incidents")
        );
    }

    @Test
    void shouldThrowWhenSavedViewNameAlreadyExistsForOwnerAndType() {
        CreateSavedViewRequestDto request = buildRequest();

        when(savedViewRepository.existsByOwnerEmailAndViewTypeAndNameIgnoreCase(
                "admin@nis2.com",
                SavedViewType.INCIDENTS,
                "Open incidents"
        )).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> savedViewService.createSavedView(request, "admin@nis2.com")
        );
    }

    @Test
    void shouldReturnSavedViewsForOwnerAndType() {
        SavedView savedView = buildSavedView();

        when(savedViewRepository.findByOwnerEmailAndViewTypeOrderByUpdatedAtDesc(
                "admin@nis2.com",
                SavedViewType.INCIDENTS
        )).thenReturn(List.of(savedView));

        List<SavedViewResponseDto> response = savedViewService.getSavedViews(
                SavedViewType.INCIDENTS,
                "admin@nis2.com"
        );

        assertEquals(1, response.size());
        assertEquals("Open incidents", response.get(0).getName());
    }

    @Test
    void shouldDeleteSavedViewSuccessfully() {
        SavedView savedView = buildSavedView();

        when(savedViewRepository.findByIdAndOwnerEmail(1L, "admin@nis2.com"))
                .thenReturn(Optional.of(savedView));

        SavedViewResponseDto response = savedViewService.deleteSavedView(1L, "admin@nis2.com");

        assertEquals(1L, response.getId());
        verify(savedViewRepository).delete(savedView);
        verify(auditLogService).record(
                eq("SAVED_VIEW_DELETED"),
                eq("SAVED_VIEW"),
                eq(1L),
                eq("admin@nis2.com"),
                eq("Saved view deleted: Open incidents")
        );
    }

    @Test
    void shouldThrowWhenDeletingMissingSavedView() {
        when(savedViewRepository.findByIdAndOwnerEmail(99L, "admin@nis2.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> savedViewService.deleteSavedView(99L, "admin@nis2.com")
        );
    }

    private CreateSavedViewRequestDto buildRequest() {
        CreateSavedViewRequestDto request = new CreateSavedViewRequestDto();
        request.setViewType(SavedViewType.INCIDENTS);
        request.setName(" Open incidents ");
        request.setFilterJson(" {\"status\":\"OPEN\"} ");
        return request;
    }

    private SavedView buildSavedView() {
        SavedView savedView = new SavedView();
        savedView.setId(1L);
        savedView.setOwnerEmail("admin@nis2.com");
        savedView.setViewType(SavedViewType.INCIDENTS);
        savedView.setName("Open incidents");
        savedView.setFilterJson("{\"status\":\"OPEN\"}");
        savedView.setCreatedAt(LocalDateTime.now());
        savedView.setUpdatedAt(LocalDateTime.now());
        return savedView;
    }
}
