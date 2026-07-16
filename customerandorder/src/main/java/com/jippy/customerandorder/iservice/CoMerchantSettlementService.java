package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoMerchantSettlementOutletDto;
import com.jippy.customerandorder.dto.CoMerchantSettlementRequestDto;

import java.time.LocalDate;
import java.util.List;

public interface CoMerchantSettlementService {

    //  Get merchant settlement details
    List<CoMerchantSettlementOutletDto>
    getProductDetailsForMerchantSettlement(LocalDate startDate, LocalDate endDate);
}

