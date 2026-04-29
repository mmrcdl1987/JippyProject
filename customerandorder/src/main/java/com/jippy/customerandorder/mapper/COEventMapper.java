package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.Constants.COConstants;
import com.jippy.customerandorder.dto.COOrderEvent;
import com.jippy.customerandorder.entity.CoOrder;

public class COEventMapper {

    public static COOrderEvent mapToOrderEvent(CoOrder order){
        COOrderEvent event = new COOrderEvent();

        event.setOrderId(order.getOrderId());
        event.setCustomerId(order.getCustomerId());
        event.setOutletId(order.getOutletId());
        event.setDriverId(order.getDriverId());
        event.setStatus(COConstants.ORDER_STATUS_PLACED);
        return event;
    }
}
