package com.jippy.customerandorder.iservice;
import com.jippy.customerandorder.dto.CoCartReminderDto;
import com.jippy.customerandorder.dto.CoCartResponseDto;
import com.jippy.customerandorder.dto.CoCartUpdateRequestDto;

import java.util.List;

public interface ICartService {

    String saveOrUpdateCart(CoCartUpdateRequestDto dto);

    CoCartResponseDto getCart(Integer customerId);

    List<CoCartReminderDto> getCartReminderCustomers();
    void processCartReminders();

}
