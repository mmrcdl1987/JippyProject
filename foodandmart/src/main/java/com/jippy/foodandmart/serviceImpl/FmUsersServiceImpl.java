package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmPasswordResetByAdminRequestDto;
import com.jippy.foodandmart.dto.FmUserDto;
import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmMerchantMapper;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.service.IFmUsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class FmUsersServiceImpl implements IFmUsersService {

    @Autowired
    private FmUserRepository usersRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;


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

    //  for creating user in FM microservice, we will receive the user details from
    //  CO microservice and then we will save the user details in FM microservice users table
    @Override
    public FmUserDto createUser(FmUserDto dto) {

//  checking whether username with same role already exists or not
        Optional<FmUser> existingUser = usersRepo.findByUsernameAndUserType
                (dto.getUsername(), dto.getUserType());

        if (existingUser.isPresent()) {
            throw new ResourceNotFoundException("Username already exists with this role." +
                    " Please try a different username.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        FmUser user = FmMerchantMapper.toUserEntity(dto.getUsername(), encodedPassword, dto.getUserId(), dto.getUserType());
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(dto.getUserId()); // userid = driverid by CO MicroService

        FmUser savedUser = usersRepo.save(user);

        FmUserDto userDto = new FmUserDto();
        userDto.setUserId(savedUser.getUserId());
        userDto.setUserType(savedUser.getUserType());
        userDto.setUsername(savedUser.getUsername());
        userDto.setPassword(savedUser.getPassword());

        return userDto;
    }

    @Override
    public String passwordResetByAdminForRoles (FmPasswordResetByAdminRequestDto dto) {

    log.info("Admin password reset initiated for username: {}, userType: {}", dto.getUsername(),
                dto.getUserType());

        FmUser user = usersRepo.findByUsernameAndUserType(dto.getUsername(), dto.getUserType())
                .orElseThrow(() -> {
                    log.error("User not found with username: {} and userType: {}", dto.getUsername(),
                            dto.getUserType());

                    return new ResourceNotFoundException("User not found with username: "
                                    + dto.getUsername() + " and userType: " + dto.getUserType());
                });
//       password Encoding
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());

//        setting new encoded[reset] password in to the user's Table
        user.setPassword(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());
        usersRepo.save(user);

        log.info("Password reset successful for username: {}", dto.getUsername());

        return "Password reset successful for your UserName :" + dto.getUsername()
                +" for your Role :" + dto.getUserType();

    }

}