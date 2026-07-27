package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmCreateEmployeeDto;
import com.jippy.foodandmart.dto.FmPasswordResetByAdminRequestDto;
import com.jippy.foodandmart.dto.FmUserDto;
import com.jippy.foodandmart.dto.FmUserResponseDto;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmMerchantMapper;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.IFmUsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FmUsersServiceImpl implements IFmUsersService {

    @Autowired
    private FmUserRepository usersRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FmRoleRepository roleRepository;

    @Autowired
    private FmRolePermissionsRepository rolePermissionsRepository;

    @Autowired
    private FmUserRolesRepository userRolesRepository;

    @Autowired
    private FmEmployeeRepository employeeRepository;


    // -------------------------------
    // LOGIC: UPDATE is_active = 'N'
    // -------------------------------
    @Override
    public void deactivateDriver(Integer userId) {

        // Find driver user
        Optional<FmUser> user = usersRepo.findByUserIdAndUserType(userId, "DRIVER");

        if (!user.isPresent()) {
            throw new RuntimeException("Driver not found in users table");
        }

        FmUser existingUser = user.get();
        //for updating the user record, we will set is_active = 'N' when orders lock in Co wallet table = false
        existingUser.setIsActive("N");

        existingUser.setUpdatedAt(LocalDateTime.now());

        usersRepo.save(existingUser);
    }

    //  for creating user in FM microservice, we will receive the user details from
    //  CO microservice and then we will save the user details in FM microservice users table
    @Override
    @Transactional
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

        FmRoles role = new FmRoles();
        log.info("=-========================{}",dto.getUserType());

        if(dto.getUserType().equals(FmAppConstants.TYPE_DRIVER)){
            role = roleRepository.findByRoleName(FmAppConstants.ROLE_DRIVER);
            if (role == null) {
                throw new RuntimeException("Role not found");
            }
        }else if(dto.getUserType().equals(FmAppConstants.TYPE_CUSTOMER)){
            log.info("=-========================{}",dto.getUserType());
            role = roleRepository.findByRoleName(FmAppConstants.ROLE_CUSTOMER);
            if (role == null) {
                throw new RuntimeException("Role not found");
            }
            log.info("=-========================{}",role.getRoleName());
        }

        //  Fetch role_permissions
        List<FmRolePermissions> rolePermissionsList = rolePermissionsRepository.findByRole(role);

        if (rolePermissionsList.isEmpty()) {
            throw new RuntimeException("No permissions mapped to role");
        }
        log.info("=-========================{}",rolePermissionsList.size());
        //  Map user → role_permissions
        for (FmRolePermissions rp : rolePermissionsList) {
            FmUserRolePermissions urp = FmMerchantMapper.toUserRolesEntity(savedUser, rp);
            userRolesRepository.save(urp);
            log.info("=-========================{}",urp.getUserId());
        }

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

    @Override
    public FmUserDto findByUserIdAndUserType(Integer userId, String userType) {

        log.info("findByUserIdAndUserType API called with userId: {}, userType:{} ", userId,
                userType);
        Optional<FmUser> user = usersRepo.findByUserIdAndUserType(userId,userType);
               /* .orElseThrow(() -> {
                    log.error("User not found with userId: {} and userType: {}", userId,
                            userType);

                    return new ResourceNotFoundException("User not found with username: "
                           +userId + " and userType: " +userType);
                });*/
        FmUserDto userDto = new FmUserDto();
        if(!user.isPresent()){
            return userDto;
        }

        userDto.setUserId(user.get().getUsersId());
        userDto.setUserId(user.get().getUserId());
        userDto.setUserType(user.get().getUserType());
        userDto.setUsername(user.get().getUsername());
        userDto.setIsActive(user.get().getIsActive());

        return  userDto;
    }

    @Override
    @Transactional
    public void assignRolesToUser(Integer userId, List<Integer> roleIds) {

        FmUser user = usersRepo.findByUsersId(userId).orElseThrow(() -> new RuntimeException("User not found"));

        userRolesRepository.deleteByUserId(userId);

        for (Integer roleId : roleIds) {

            FmRoles role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));

            List<FmRolePermissions> rolePermissions = rolePermissionsRepository.findByRole(role);

            for (FmRolePermissions rp : rolePermissions) {

                FmUserRolePermissions urp = new FmUserRolePermissions();

                urp.setUserId(userId);

                urp.setRolePermission(rp);

                urp.setCreatedAt(LocalDateTime.now());

                urp.setCreatedBy(1);

                userRolesRepository.save(urp);
            }
        }
    }

    @Override
    public List<FmUserResponseDto> getAllUsers() {

        List<FmUser> users = usersRepo.findByUserType(FmAppConstants.TYPE_EMPLOYEE);

        return users.stream().map(user -> {

            FmUserResponseDto dto = new FmUserResponseDto();

            dto.setUsersId(user.getUsersId());
            dto.setUserId(user.getUserId());
            dto.setUsername(user.getUsername());
            dto.setUserType(user.getUserType());

            String roleName = user.getUserRolePermissions().stream().map(urp -> urp.getRolePermission().getRole().getRoleName()).distinct().collect(Collectors.joining(", "));

            dto.setRoleName(roleName);

            return dto;
        }).toList();
    }

    @Override
    public List<Integer> getUserRoleIds(Integer userId) {
        return userRolesRepository.findRoleIdsByUserId(userId);
    }

    @Override
    @Transactional
    public void createEmployee(FmCreateEmployeeDto dto) {

        log.info("Employee Creation Started | username={} | email={}", dto.getUsername(), dto.getEmail());

        try {

            FmEmployee employee = new FmEmployee();

            employee.setEmployeeName(dto.getEmployeeName());

            employee.setEmail(dto.getEmail());

            employee.setMobileNumber(dto.getMobileNumber());

            employee.setIsActive("Y");

            employee = employeeRepository.save(employee);

            log.info("Employee Record Created | employeeId={} | email={}", employee.getEmployeeId(), employee.getEmail());

            FmUser user = new FmUser();

            user.setUsername(dto.getUsername());

            user.setPassword(passwordEncoder.encode(dto.getPassword()));

            user.setUserId(employee.getEmployeeId());

            user.setUserType("EMPLOYEE");

            user.setIsActive("Y");

            user = usersRepo.save(user);

            log.info("User Record Created | usersId={} | username={} | userType={}", user.getUsersId(), user.getUsername(), user.getUserType());

            log.info("Employee Creation Completed Successfully | employeeId={} | usersId={}", employee.getEmployeeId(), user.getUsersId());

        } catch (Exception ex) {

            log.error("Employee Creation Failed | username={} | email={} | error={}", dto.getUsername(), dto.getEmail(), ex.getMessage(), ex);

            throw ex;
        }
    }
//    ------------USED FOR APPROVALS--------------------------
    /**
     * Activates the User based on Entity Type and Entity Id.
     *
     * <p>
     * Business Rules:
     *
     * 1. Entity Type must be OUTLET, MERCHANT or DRIVER.
     * 2. User must exist for the given Entity Id and Entity Type.
     * 3. User Status will be updated from N to Y.
     * 4. Updated By will contain the Approver Id.
     *
     * @param entityType Entity Type
     * @param entityId   Entity Id
     * @param approverId Approver Id
     */
    @Override
    public void activateUser(
            String entityType,
            Integer entityId,
            Integer approverId) {

        log.info(
                "Started User Activation. Entity Type : {}, Entity Id : {}, Approver Id : {}",
                entityType,
                entityId,
                approverId);

        //----------------------------------------------------------
        // Validate Supported Entity Type
        //----------------------------------------------------------

        if (!FmAppConstants.TYPE_OUTLET.equalsIgnoreCase(entityType)
                && !FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(entityType)
                && !FmAppConstants.TYPE_DRIVER.equalsIgnoreCase(entityType)) {

            log.error(
                    "Unsupported Entity Type for User Activation : {}",
                    entityType);

            throw new IllegalArgumentException(
                    FmAppConstants.MSG_UNSUPPORTED_ENTITY_TYPE + entityType);
        }

        //----------------------------------------------------------
        // Activate User
        //----------------------------------------------------------

        int updatedRows = usersRepo.activateUser(entityId, entityType, approverId);

        //----------------------------------------------------------
        // Validate Update
        //----------------------------------------------------------

        if (updatedRows == 0) {

            log.warn("No User found for activation. Entity Type : {}, Entity Id : {}",
                    entityType, entityId);

            return;
        }

        //----------------------------------------------------------
        // User Activated Successfully
        //----------------------------------------------------------

        log.info("User Activated Successfully. Entity Type : {}, Entity Id : {}, Updated By : {}",
                entityType, entityId, approverId);
    }
}