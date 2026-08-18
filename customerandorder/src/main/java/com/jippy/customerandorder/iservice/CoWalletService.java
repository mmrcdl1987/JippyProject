package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoCustomerWalletResponseDto;
import com.jippy.customerandorder.entity.CoCustomerWallet;

public interface CoWalletService {

    CoCustomerWalletResponseDto getByCustomerId(Integer customerId);

    CoCustomerWallet updateByCustomerId(
            Integer customerId,
            CoCustomerWallet walletDetails
    );
}
