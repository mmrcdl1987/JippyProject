package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomer;

public interface ICoCustomerService {

    CoCustomer createCustomer(CoCustomerRequestDto dto);

    CoWalletResponseDto convertPoints(Integer customerId);

    // CoCustomerStreakResponseDto updateDailyStreak(Integer customerId);
    CoCustomerStreakResponseDto updateDailyStreak(Integer customerId);

    CoWalletTransferResponseDto transferWalletPoints(CoWalletTransferRequestDto requestDto);

    CoCustomerResponseDto getCustomer(Integer customerId);

    CoCustomerResponseDto updateCustomer(Integer customerId, CoCustomerRequestDto requestDto
    );
}