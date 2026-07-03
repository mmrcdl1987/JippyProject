package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FmPermissionRepository
        extends JpaRepository<FmPermission,Integer> {
}
