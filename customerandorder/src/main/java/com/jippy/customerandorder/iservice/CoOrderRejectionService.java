package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderRejectionRequestDto;
import com.jippy.customerandorder.entity.CoOrderRejection;

/**
 * Service Interface
 */
public interface CoOrderRejectionService {

    CoOrderRejection rejectOrder(
            CoOrderRejectionRequestDto request
    );
}