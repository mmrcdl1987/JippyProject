package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductVariantGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FmProductVariantGroupRepository
        extends JpaRepository<FmProductVariantGroup, Integer> {

    /**
     * Get all active groups
     */
    List<FmProductVariantGroup> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find by id and active
     */
    Optional<FmProductVariantGroup> findByProductVariantGroupsIdAndIsActiveTrue(
            Integer productVariantGroupsId);

    /**
     * Duplicate validation
     */
    boolean existsByGroupNameIgnoreCase(String groupName);

    /**
     * Duplicate validation while update
     */
    boolean existsByGroupNameIgnoreCaseAndProductVariantGroupsIdNot(
            String groupName,
            Integer productVariantGroupsId);
}