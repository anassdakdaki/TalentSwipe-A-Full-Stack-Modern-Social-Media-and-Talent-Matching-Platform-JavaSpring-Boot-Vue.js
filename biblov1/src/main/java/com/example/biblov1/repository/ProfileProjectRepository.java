package com.example.biblov1.repository;

import com.example.biblov1.model.ProfileProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileProjectRepository extends JpaRepository<ProfileProject, Long> {
    List<ProfileProject> findByProfileIdOrderBySortOrderAscIdAsc(Long profileId);
}