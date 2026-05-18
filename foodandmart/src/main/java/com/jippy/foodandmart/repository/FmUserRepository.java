package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmUser;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmUserRepository extends JpaRepository<FmUser, Integer> {
    Optional<FmUser> findByUsername(String username);
    boolean existsByUsername(String username);
    //List<RolePerissions> findByRoleId(Integer roleId);

    // -------------------------------
    // FIND DRIVER BY user_id + user_type
    // -------------------------------
    FmUser findByUserIdAndUserType(Integer userId, String userType);
}
