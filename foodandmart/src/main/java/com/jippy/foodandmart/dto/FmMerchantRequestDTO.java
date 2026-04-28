package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMerchantRequestDTO {

    // ── Required Fields ──────────────────────────────────────────────────────

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 75, message = "First name must be between 2 and 75 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name must contain only letters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 75, message = "Last name must be between 2 and 75 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name must contain only letters")
    private String lastName;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2}|\\d{2}-\\d{2}-\\d{2})$",
            message = "DOB must be in YYYY-MM-DD or MM-DD-YY format")
    private String dob;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String phone;

    @NotBlank(message = "Outlet type is required")
    @Size(max = 50, message = "Outlet type must not exceed 50 characters")
    private String outletType;

    // optional — no @NotBlank
    @Size(max = 100, message = "UploadedBy must not exceed 100 characters")
    private String uploadedBy;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN must be in format: AAAAA9999A")
    private String pan;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Aadhaar must be a valid 12-digit number")
    private String adhar;

    @NotBlank(message = "FSSAI number is required")
    @Pattern(regexp = "^\\d{14}$", message = "FSSAI must be a 14-digit number")
    private String fssai;

    // ── Optional Fields ───────────────────────────────────────────────────────

    /** Optional GST number — validated if provided */
    @Pattern(regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "GST must be a valid 15-character GSTIN")
    private String gstNumber;

    @Pattern(regexp = "^$|^[0-9]{9,18}$", message = "Account number must be 9–18 digits")
    private String accountNumber;

    @Pattern(regexp = "^$|^[A-Z]{4}0[A-Z0-9]{6}$", message = "IFSC must be in format: ABCD0123456")
    private String ifscCode;

    @Size(max = 100, message = "Bank location must not exceed 100 characters")
    private String bankLocation;

    @Size(max = 150, message = "Name in bank account must not exceed 150 characters")
    private String nameInBankAccount;
}
