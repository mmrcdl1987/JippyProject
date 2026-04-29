package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FmRoleRepository extends JpaRepository<FmRoles,Integer> {
    FmRoles findByRoleName(String typeMerchant);
}