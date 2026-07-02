package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmUser;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmUserRepository extends JpaRepository<FmUser, Integer> {
    Optional<FmUser> findByUsername(String username);
    boolean existsByUsername(String username);
    //List<RolePerissions> findByRoleId(Integer roleId);

    // -------------------------------
    // FIND DRIVER BY user_id + user_type
    // -------------------------------
//    used for api for "updateCODAmountByFleetManager" also
    Optional<FmUser> findByUserIdAndUserType(Integer userId, String userType);

//    1)used for PasswordResetByAdminRoles - Api
//    2)checks if already found to prevent unique
//    username and unique user type for 1)MERCHANT 2)OUTLET 3)DRIVER roles
    Optional<FmUser> findByUsernameAndUserType(String username, String userType);


    Optional<FmUser> findByUsersId(Integer usersId);

    List<FmUser> findByUserType(String userType);
}
