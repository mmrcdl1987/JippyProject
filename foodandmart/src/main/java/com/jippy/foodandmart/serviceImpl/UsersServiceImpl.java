package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.service.IFmUsersService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UsersServiceImpl implements IFmUsersService {

    @Autowired
    private FmUserRepository usersRepo;

    // -------------------------------
    // LOGIC: UPDATE is_active = 'N'
    // -------------------------------
    @Override
    public void deactivateDriver(Integer userId) {

        // Find driver user
        FmUser user = usersRepo.findByUserIdAndUserType(userId, "DRIVER");

        if (user == null) {
            throw new RuntimeException("Driver not found in users table");
        }

        //for updating the user record, we will set is_active = 'N' when orders lock in Co wallet table = false
        user.setIsActive("N");

        user.setUpdatedAt(LocalDateTime.now());

        usersRepo.save(user);
    }
}