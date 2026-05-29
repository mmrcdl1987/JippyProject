package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoMerchantSettlementOutletDto;
import com.jippy.customerandorder.dto.CoMerchantSettlementRequestDto;

import java.util.List;

public interface CoMerchantSettlementService {

    //  Get merchant settlement details
    List<CoMerchantSettlementOutletDto>
    getProductDetailsForMerchantSettlement(CoMerchantSettlementRequestDto dto);
}

