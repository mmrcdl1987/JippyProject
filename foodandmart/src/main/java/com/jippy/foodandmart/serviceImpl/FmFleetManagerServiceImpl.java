package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.DriverWalletUpdateResponseDto;
import com.jippy.foodandmart.dto.FmUpdateCODResponseDto;
import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DriverFeignClient;
import com.jippy.foodandmart.mapper.FmFleetManagerMapper;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.service.FmFleetManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmFleetManagerServiceImpl implements FmFleetManagerService {

    private final DriverFeignClient driverFeignClient;
    private final FmUserRepository fmUsersRepository;

    @Override
    public FmUpdateCODResponseDto updateCODAmountByFleetManager(Integer driverId, Integer fleetManagerId) {

        log.info("Updating COD amount for driverId : {}", driverId);

//        data came from Driver DTO
        DriverWalletUpdateResponseDto walletResponse;
        try {
            walletResponse = driverFeignClient.updateCODAmountByFleetManager(driverId, fleetManagerId);
        }
        catch (Exception e) {
            log.error("Failed to call Driver service", e);
            throw new ResourceNotFoundException("Driver service is unavailable");
        }

        log.info("Amount received from Driver Service = {}", walletResponse.getUpdatedCodAmount());

        Optional<FmUser> user = fmUsersRepository.findByUserIdAndUserType(driverId, "DRIVER");

        if (!user.isPresent()) {
            throw new ResourceNotFoundException("Driver not found for driverId : " + driverId);
        }
        FmUser existingUser = user.get();
        log.info("User status before update = {}", existingUser.getIsActive());

        existingUser.setIsActive(FmAppConstants.FLAG_YES);
        existingUser.setUpdatedAt(LocalDateTime.now());

        FmUser savedUser = fmUsersRepository.save(existingUser);

        log.info("After Save User Status = {}", savedUser.getIsActive());

        log.info("Driver activated successfully for driverId : {}", driverId);

        FmUpdateCODResponseDto response = FmFleetManagerMapper.toUpdateCODResponseDto(
                walletResponse, savedUser.getIsActive());

        return response;
    }
}
