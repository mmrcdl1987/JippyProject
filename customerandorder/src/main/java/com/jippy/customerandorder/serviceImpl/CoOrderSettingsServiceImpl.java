package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.dto.CoPaymentModeResponse;
import com.jippy.customerandorder.entity.CoPaymentModes;
import com.jippy.customerandorder.iservice.OrderSettingsService;
import com.jippy.customerandorder.repository.CoPaymentModeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoOrderSettingsServiceImpl implements OrderSettingsService {

    private final CoPaymentModeRepository paymentModeRepository;

    @Override
    public CoOrderSettingsResponseDto saveOrUpdate(CoOrderSettingsRequestDto requestDto) {
        return null;
    }

    @Override
    public CoPaymentModeResponse getPaymentModeById(Integer paymentModeId) {

        Optional<CoPaymentModes> paymentModesOptional = paymentModeRepository.findByPaymentModeId(paymentModeId);
       CoPaymentModeResponse paymentModeResponse =new CoPaymentModeResponse();

       if(paymentModesOptional.isPresent()){
           CoPaymentModes paymentModes = paymentModesOptional.get();

           paymentModeResponse.setPaymentModeId(paymentModes.getPaymentModeId());
           paymentModeResponse.setPaymentMode(paymentModes.getPaymentMode());

           return paymentModeResponse;
       }
        return paymentModeResponse;

    }

    @Override
    public List<CoPaymentModeResponse> getActivePaymentModes() {
        String isActive = "Y";
       List<CoPaymentModes> paymentModesList = paymentModeRepository.findByIsActive(isActive);

        List<CoPaymentModeResponse> paymentModeResponseList = new ArrayList<>();
       for(CoPaymentModes paymentModes : paymentModesList){

           CoPaymentModeResponse coPaymentModeResponse = new CoPaymentModeResponse();

           coPaymentModeResponse.setPaymentModeId(paymentModes.getPaymentModeId());
           coPaymentModeResponse.setPaymentMode(paymentModes.getPaymentMode());
           coPaymentModeResponse.setIsActive(paymentModes.getIsActive());

           paymentModeResponseList.add(coPaymentModeResponse);
       }
        return paymentModeResponseList;
    }
}
