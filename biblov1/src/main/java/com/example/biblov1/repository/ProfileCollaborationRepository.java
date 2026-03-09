package com.example.biblov1.repository;

import com.example.biblov1.model.ProfileCollaboration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileCollaborationRepository extends JpaRepository<ProfileCollaboration, Long> {
    List<ProfileCollaboration> findByProfileIdOrderBySortOrderAscIdAsc(Long profileId);
}