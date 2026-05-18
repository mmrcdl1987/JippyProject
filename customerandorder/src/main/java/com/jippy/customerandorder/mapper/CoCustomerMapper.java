package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoCustomerRequestDto;
import com.jippy.customerandorder.dto.CoCustomerResponseDto;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.CoCustomerWallet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CoCustomerMapper {

    private CoCustomerMapper() {
    }
    // CUSTOMER MAPPER


    public static CoCustomer mapToCustomer(CoCustomerRequestDto dto) {

        CoCustomer customer = new CoCustomer();

        customer.setFirstName(dto.getFirstName());

        customer.setLastName(dto.getLastName());

        customer.setEmail(dto.getEmail());

        customer.setPhoneNumber(dto.getPhoneNumber());

        customer.setCustomerStatusId(1);

        customer.setReferralCode(generateReferralCode(

                dto.getFirstName(),

                dto.getLastName(),

                dto.getPhoneNumber()));

        customer.setCreatedAt(LocalDateTime.now());

        customer.setCreatedBy(dto.getCreatedBy());

        return customer;
    }


    // WALLET MAPPER


    public static CoCustomerWallet mapToWallet(CoCustomer customer, Integer balancePoints, Integer createdBy) {

        CoCustomerWallet wallet = new CoCustomerWallet();

        wallet.setCustomer(customer);

        wallet.setBalancePoints(balancePoints);

        wallet.setBalanceAmount(BigDecimal.ZERO);

        wallet.setCreatedAt(LocalDateTime.now());

        wallet.setCreatedBy(createdBy);

        return wallet;
    }


    // REFERRAL CODE GENERATOR


    private static String generateReferralCode(String firstName, String lastName, String phoneNumber) {

        String firstPart = firstName.substring(0, Math.min(3, firstName.length()));

        String lastPart = lastName.substring(0, Math.min(2, lastName.length()));

        String phoneLast3 = phoneNumber.substring(phoneNumber.length() - 3);

        return (firstPart + phoneLast3 + lastPart).toUpperCase();
    }

    public CoCustomerResponseDto mapToResponse(CoCustomer customer) {

        CoCustomerResponseDto dto =
                new CoCustomerResponseDto();

        dto.setCustomerId(customer.getCustomerId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setReferralCode(customer.getReferralCode());
        dto.setCustomerStatusId(customer.getCustomerStatusId());

        return dto;
    }
}