package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.config.FmPasswordConfig;
import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.MerchantAlreadyExistsException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmMerchantMapper;
import com.jippy.foodandmart.projections.FmMerchantWithBankProjection;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.IFmMerchantService;
import com.jippy.foodandmart.validation.FmFileParser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service implementation for merchant onboarding and bulk CSV/Excel upload.
 *
 * <p>Layer order: Controller → IMerchantService → MerchantServiceImpl → MerchantMapper → Repository → Entity.</p>
 *
 * <p>Self-injection via {@code @Lazy} is required so that {@code bulkUpload} can route
 * each row through the Spring AOP proxy, ensuring the {@code REQUIRES_NEW} transaction
 * on {@code createMerchant} is honoured per row.</p>
 */
@Service
@Slf4j
public class FmMerchantServiceImpl implements IFmMerchantService {

    private final FmMerchantRepository merchantRepository;
    private final FmMerchantKycRepository merchantKycRepository;
    private final FmMerchantBankDetailsRepository bankDetailsRepository;
    private final FmUserRepository userRepository;
    private final Validator validator;
    private final FmUserRolesRepository userRolesRepository;
    private final FmRoleRepository roleRepository;
    private final FmRolePermissionsRepository rolePermissionsRepository;
    private final PasswordEncoder passwordEncoder;
    @Lazy
    @Autowired
    private IFmMerchantService self;

    @Autowired
    public FmMerchantServiceImpl(FmMerchantRepository merchantRepository, FmMerchantKycRepository merchantKycRepository, FmMerchantBankDetailsRepository bankDetailsRepository, FmUserRepository userRepository, FmEmployeeRepository employeeRepository, Validator validator, FmRoleRepository roleRepository, FmUserRolesRepository userRolesRepository, FmRolePermissionsRepository rolePermissionsRepository, PasswordEncoder passwordEncoder) {
        this.merchantRepository = merchantRepository;
        this.merchantKycRepository = merchantKycRepository;
        this.bankDetailsRepository = bankDetailsRepository;
        this.userRepository = userRepository;
        this.validator = validator;
        this.roleRepository = roleRepository;
        this.userRolesRepository = userRolesRepository;
        this.rolePermissionsRepository = rolePermissionsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public List<FmMerchant> getAllMerchants() {
        return merchantRepository.findAll();
    }

    @Override
    public FmMerchantDto getMerchantById(Integer id) {
        FmMerchant merchant = merchantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Merchant ID " + id + " does not exist"));
        FmMerchantDto dto = FmMerchantMapper.mapToMerchantDto(merchant);
        log.info("[MERCHANT] Fetched by ID: merchantId={}, name={}", id, merchant.getMerchantName());
        return dto;
    }

    @Override
    public long countMerchants() {
        long count = merchantRepository.count();
        log.info("[MERCHANT] Count fetched: {}", count);
        return count;
    }

    // ── Single Create ─────────────────────────────────────────────────────────

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FmMerchant createMerchant(FmMerchantRequestDTO dto) {
        log.info("[MERCHANT] Creating merchant: email={}, phone={}", dto.getEmail(), dto.getPhone());

        validateUniqueness(dto.getEmail(), dto.getPhone(), dto.getPan(), dto.getAdhar(), dto.getFssai(), dto.getAccountNumber());

        FmMerchant merchant = FmMerchantMapper.toEntity(dto);
        merchant = merchantRepository.save(merchant);
        log.info("[MERCHANT] Saved: merchantId={}, name={}", merchant.getMerchantId(), merchant.getMerchantName());

        saveKyc(dto, merchant);
        saveBankDetails(dto, merchant.getMerchantId());
        createMerchantUser(dto, merchant.getMerchantId());

        log.info("[MERCHANT] Onboarding complete: merchantId={}", merchant.getMerchantId());
        return merchant;
    }


    // ── Bulk Upload ───────────────────────────────────────────────────────────

    @Override
    public FmBulkUploadResultDTO bulkUpload(MultipartFile file) {
        log.info("[BULK] Starting bulk upload: filename={}, size={} bytes", file.getOriginalFilename(), file.getSize());

        List<FmMerchantRequestDTO> rows;
        try {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rows = FmFileParser.parseExcel(file);
            } else if (filename.endsWith(".csv")) {
                rows = FmFileParser.parseCsv(file);
            } else {
                return buildErrorResult(0, "Unsupported file type. Only CSV and Excel files are allowed.");
            }
        } catch (Exception e) {
            log.error("[BULK] File parsing failed: {}", e.getMessage(), e);
            return buildErrorResult(0, "File parsing failed: " + e.getMessage());
        }

        log.info("[BULK] Parsed {} rows from file", rows.size());

        int success = 0, failure = 0;
        List<FmBulkUploadResultDTO.RowErrorDTO> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            FmMerchantRequestDTO dto = rows.get(i);
            int rowNum = i + 2;
            try {
                Set<ConstraintViolation<FmMerchantRequestDTO>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    violations.forEach(v -> errors.add(FmBulkUploadResultDTO.RowErrorDTO.builder().rowNumber(rowNum).field(v.getPropertyPath().toString()).value(v.getInvalidValue() != null ? v.getInvalidValue().toString() : "").reason(v.getMessage()).build()));
                    failure++;
                    continue;
                }
                FmMerchant merchant = self.createMerchant(dto);
                success++;
                log.info("[BULK] Row {} saved: merchantId={}", rowNum, merchant.getMerchantId());
            } catch (MerchantAlreadyExistsException e) {
                log.warn("[BULK] Row {} skipped (duplicate): {}", rowNum, e.getMessage());
                errors.add(FmBulkUploadResultDTO.RowErrorDTO.builder().rowNumber(rowNum).field("duplicate").value("").reason(e.getMessage()).build());
                failure++;
            } catch (Exception e) {
                log.error("[BULK] Row {} failed: {}", rowNum, e.getMessage(), e);
                errors.add(FmBulkUploadResultDTO.RowErrorDTO.builder().rowNumber(rowNum).field("unknown").value("").reason(e.getMessage()).build());
                failure++;
            }
        }

        log.info("[BULK] Complete: total={}, success={}, failure={}", rows.size(), success, failure);
        return FmBulkUploadResultDTO.builder().totalRows(rows.size()).successCount(success).failureCount(failure).errors(errors).build();
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private void validateUniqueness(String email, String phone, String pan, String aadhaar, String fssai, String accountNumber) {
        if (merchantRepository.existsByMerchantEmail(email))
            throw new MerchantAlreadyExistsException("Email already registered: " + email);
        if (merchantRepository.existsByMerchantPhone(phone))
            throw new MerchantAlreadyExistsException("Phone already registered: " + phone);
        if (pan != null && merchantKycRepository.existsByPanNumber(pan))
            throw new MerchantAlreadyExistsException("PAN already registered: " + pan);
        if (aadhaar != null && merchantKycRepository.existsByAadhaarNumber(aadhaar))
            throw new MerchantAlreadyExistsException("Aadhaar already registered: " + aadhaar);
        if (fssai != null && merchantKycRepository.existsByFssaiNumber(fssai))
            throw new MerchantAlreadyExistsException("FSSAI already registered: " + fssai);
        if (accountNumber != null && !accountNumber.isBlank() && bankDetailsRepository.existsByAccountNumber(accountNumber))
            throw new MerchantAlreadyExistsException("Account number already registered: " + accountNumber);
    }

    private void saveKyc(FmMerchantRequestDTO dto, FmMerchant merchant) {
        FmMerchantKyc kyc = FmMerchantMapper.toKycEntity(dto, merchant);
        merchantKycRepository.save(kyc);
        log.info("[KYC] Saved for merchantId={}", merchant.getMerchantId());
    }

    private void saveBankDetails(FmMerchantRequestDTO dto, Integer merchantId) {
        boolean hasBankData = (dto.getAccountNumber() != null && !dto.getAccountNumber().isBlank()) || (dto.getIfscCode() != null && !dto.getIfscCode().isBlank());
        if (!hasBankData) return;

        FmMerchantBankDetails bank = FmMerchantMapper.toBankEntity(dto, merchantId);
        bankDetailsRepository.save(bank);
        log.info("[BANK] Details saved for merchantId={}", merchantId);
    }

    private void createMerchantUser(FmMerchantRequestDTO dto, Integer merchantId) {

        String username = dto.getUsername().trim();

        Optional<FmUser> existingUser =
                userRepository.findByUsernameAndUserType(
                        username,
                        FmAppConstants.TYPE_MERCHANT
                );

        if (existingUser.isPresent()) {
            throw new MerchantAlreadyExistsException(
                    "Username already exists."
            );
        }

        String encodedPassword =
                passwordEncoder.encode(dto.getPassword());

        FmUser user =
                FmMerchantMapper.toUserEntity(
                        username,
                        encodedPassword,
                        merchantId,
                        FmAppConstants.TYPE_MERCHANT
                );

        user = userRepository.save(user);

        FmRoles role = roleRepository.findByRoleName(
                FmAppConstants.ROLE_MERCHANT
        );

        if (role == null) {
            throw new ResourceNotFoundException("Merchant role not found.");
        }

        List<FmRolePermissions> rolePermissions =
                rolePermissionsRepository.findByRole(role);

        if (rolePermissions.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No permissions mapped to Merchant role."
            );
        }

        for (FmRolePermissions permission : rolePermissions) {

            FmUserRolePermissions userRole =
                    FmMerchantMapper.toUserRolesEntity(
                            user,
                            permission
                    );

            userRolesRepository.save(userRole);
        }

        log.info("Merchant login created successfully. username={}", username);
    }

    private FmBulkUploadResultDTO buildErrorResult(int rowNum, String reason) {
        return FmBulkUploadResultDTO.builder().totalRows(0).successCount(0).failureCount(0).errors
                (List.of(FmBulkUploadResultDTO.RowErrorDTO.builder().rowNumber(rowNum).field("file").value("").reason(reason).build())).build();
    }

    //    get only merchant
    public FmMerchantDto getMerchantProfile(int merchantId) {
        log.info("Fetching merchant profile for merchantId: {}", merchantId);
        FmMerchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> {
            log.error("Merchant not found with id: {}", merchantId);
            return new ResourceNotFoundException("Merchant not found with id: " + merchantId);
        });

        log.info("Merchant fetched successfully for merchantId: {}", merchantId);

        return FmMerchantMapper.mapToMerchantDto(merchant);
    }

    //   get -> merchant + bank (native query)
    @Override
    public FmMerchantWithBankDto getMerchantWithBank(Integer merchantId) {
        log.info("Fetching merchant with bank details for merchantId: {}", merchantId);
        FmMerchantWithBankProjection data = merchantRepository.getMerchantWithBank(merchantId);
        if (data == null) {
            log.error("Merchant with bank details not found for merchantId: {}", merchantId);
            throw new ResourceNotFoundException("Merchant not found with :" + merchantId);
        }
        log.info("Successfully fetched merchant + bank details for merchantId: {}", merchantId);
        return FmMerchantMapper.mapToMerchantWithBankDto(data);
    }

    //    update--> merchant + bank
    @Override
    @jakarta.transaction.Transactional
    public FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto) {

        log.info("Updating merchant profile for merchantId: {}", dto.getMerchantId());
        log.debug("Request DTO: {}", dto);
        // 1. Fetch Merchant
        FmMerchant merchant = merchantRepository.findById(dto.getMerchantId().intValue()).orElseThrow(() -> {
            log.error("Merchant not found with ID: {}", dto.getMerchantId());
            return new ResourceNotFoundException("Merchant not found with ID :" + dto.getMerchantId());
        });

        // 2. Update Merchant fields by using lombok -getters and setters(data)s
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setMerchantEmail(dto.getMerchantEmail());
        merchant.setMerchantPhone(dto.getMerchantPhone());
        merchant.setMerchantBusinessType(dto.getBusinessType());
        merchant.setStatus(dto.getStatus());

        merchantRepository.save(merchant);
        log.info("Merchant updated successfully for merchantId: {}", dto.getMerchantId());

        // 3. Fetch Bank Details
        FmMerchantBankDetails bank = bankDetailsRepository.findByRecipientIdAndUserType(dto.getMerchantId(), "MERCHANT").orElseThrow(() -> new ResourceNotFoundException("Bank details not found"));

// 4. Duplicate account check
        if (!bank.getAccountNumber().equals(dto.getAccountNumber()) && bankDetailsRepository.existsByAccountNumber(dto.getAccountNumber())) {

            throw new DuplicateResourceException("Account number already exists");
        }

// 5. Update Bank fields
        bank.setAccountNumber(dto.getAccountNumber());
        bank.setIfscCode(dto.getIfscCode());
        bank.setBankName(dto.getBankName());
        bank.setAccountHolderName(dto.getAccountHolderName());
        bank.setUserType("MERCHANT");

        bankDetailsRepository.save(bank);
        log.info("Bank details updated successfully for merchantId: {}", dto.getMerchantId());

        // 6. Returning updated combined [merchant + Bank] data using mapper
        FmMerchantWithBankDto response = getMerchantWithBank(dto.getMerchantId());

        log.info("Returning updated merchant + bank response for merchantId: {}", dto.getMerchantId());

        return response;
    }

    @Override
    public FmResponseDto updateMerchantProfilePic(FmMerchantDto merchantDto) {
        log.info("Updating merchant profile picture for merchantId: {}", merchantDto.getMerchantId());
        FmMerchant merchant = merchantRepository.findById(merchantDto.getMerchantId()).orElseThrow(() -> {
            log.error("Merchant not found with ID: {}", merchantDto.getMerchantId());
            return new ResourceNotFoundException("Merchant not found with ID :" + merchantDto.getMerchantId());
        });

        merchant.setProfilePicUrl(merchantDto.getProfilePicUrl());
        merchantRepository.save(merchant);
        log.info("Merchant profile picture updated successfully for merchantId: {}", merchantDto.getMerchantId());

        return new FmResponseDto("200", "Profile picture url: " + merchantDto.getProfilePicUrl());
    }

}
