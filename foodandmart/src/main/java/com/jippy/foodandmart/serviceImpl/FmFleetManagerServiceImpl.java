package com.jippy.foodandmart.serviceImpl;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class FmFleetManagerServiceImpl implements FmFleetManagerService {

    private final DriverFeignClient driverFeignClient;
    private final FmUserRepository fmUsersRepository;

    @Override
    public FmUpdateCODResponseDto updateCODAmountByFleetManager(Integer driverId) {

        log.info("Updating COD amount for driverId : {}", driverId);

//        data came from Driver DTO
        DriverWalletUpdateResponseDto walletResponse =
                driverFeignClient.updateCODAmountByFleetManager(driverId);

        log.info("Amount received from Driver Service = {}", walletResponse.getUpdatedCodAmount());

        FmUser user = fmUsersRepository.findByUserIdAndUserType(driverId, "DRIVER");

        if (user == null) {
            throw new ResourceNotFoundException("Driver not found for driverId : " + driverId);
        }

        log.info("User status before update = {}", user.getIsActive());

        user.setIsActive("Y");
        user.setUpdatedAt(LocalDateTime.now());

        FmUser savedUser = fmUsersRepository.save(user);

        log.info("After Save User Status = {}", savedUser.getIsActive());

        log.info("Driver activated successfully for driverId : {}", driverId);

        FmUpdateCODResponseDto response = FmFleetManagerMapper.toUpdateCODResponseDto(
                walletResponse, savedUser.getIsActive());

        return response;
    }
}
