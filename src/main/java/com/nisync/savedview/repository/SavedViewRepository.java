package com.nisync.savedview.repository;

import com.nisync.savedview.entity.SavedView;
import com.nisync.savedview.enums.SavedViewType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedViewRepository extends JpaRepository<SavedView, Long> {

    List<SavedView> findByOwnerEmailAndViewTypeOrderByUpdatedAtDesc(String ownerEmail, SavedViewType viewType);

    boolean existsByOwnerEmailAndViewTypeAndNameIgnoreCase(String ownerEmail, SavedViewType viewType, String name);

    Optional<SavedView> findByIdAndOwnerEmail(Long id, String ownerEmail);
}
