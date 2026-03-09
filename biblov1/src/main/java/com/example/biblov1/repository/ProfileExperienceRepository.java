package com.example.biblov1.repository;

import com.example.biblov1.model.ProfileExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileExperienceRepository extends JpaRepository<ProfileExperience, Long> {
    List<ProfileExperience> findByProfileIdOrderBySortOrderAscIdAsc(Long profileId);
}