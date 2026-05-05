package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FmMerchantWithBankDto {

        // for merchant

        @NotNull(message = "Merchant ID cannot be null for update")
        private Long merchantId;

        @NotBlank(message = "Merchant name should not be empty")
        @Size(min = 3, max = 100, message = "Merchant name must be between 3 and 100 characters")
        private String merchantName;

        @NotBlank(message = "Merchant email should not be empty")
        @Email(message = "Invalid email format")
        private String merchantEmail;

        @NotBlank(message = "Merchant phone should not be empty")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number (must be 10 digits and start with 6-9)")
        private String merchantPhone;

        @NotBlank(message = "Business type should not be empty")
        private String businessType;

        private String status;


        // for merchant bank details

        private Long bankId;

        private Long recipientId;

        @NotBlank(message = "Account number should not be empty")
        @Pattern(regexp = "^[0-9]{9,18}$", message = "Account number must be between 9 to 18 digits")
        private String accountNumber;

        @NotBlank(message = "IFSC code should not be empty")
        @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
        private String ifscCode;

        @NotBlank(message = "Bank name should not be empty")
        private String bankName;

        @NotBlank(message = "Account holder name should not be empty")
        private String accountHolderName;

        @NotBlank(message = "User type should not be empty")
        private String userType;
}