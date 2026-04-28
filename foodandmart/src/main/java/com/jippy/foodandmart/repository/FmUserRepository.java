package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmUserRepository extends JpaRepository<FmUser, Integer> {
    Optional<FmUser> findByUsername(String username);
    boolean existsByUsername(String username);
    //List<RolePerissions> findByRoleId(Integer roleId);
}
