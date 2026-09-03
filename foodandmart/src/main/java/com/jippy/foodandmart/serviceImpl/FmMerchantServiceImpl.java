
        package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.MerchantAlreadyExistsException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmMerchantMapper;
import com.jippy.foodandmart.projections.FmMerchantWithBankProjection;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.EmailService;
import com.jippy.foodandmart.service.IFmApprovalRequestService;
import com.jippy.foodandmart.service.IFmMerchantService;
import com.jippy.foodandmart.service.S3Service;
import com.jippy.foodandmart.validation.FmFileParser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FmMerchantServiceImpl implements IFmMerchantService {

    private final FmMerchantRepository merchantRepository;
    private final FmUserKycRepository userKycRepository;
    private final FmMerchantBankDetailsRepository bankDetailsRepository;
    private final FmUserRepository userRepository;
    private final Validator validator;
    private final FmUserRolesRepository userRolesRepository;
    private final FmRoleRepository roleRepository;
    private final FmRolePermissionsRepository rolePermissionsRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final IFmApprovalRequestService approvalRequestService;
    private final S3Service s3Service;

    /*
     * ================================================================
     * BULK ADDRESS REPOSITORIES
     * ================================================================
     *
     * Used ONLY by merchant bulk upload.
     *
     * CSV/Excel contains names:
     *
     * State = Telangana
     * City  = Hyderabad
     * Area  = Kukatpally
     *
     * These are converted into:
     *
     * state_id
     * city_id
     * area_id
     */

    private final FmAddressRepository addressRepository;
    private final FmStateRepository stateRepository;
    private final FmCityRepository cityRepository;
    private final FmAreaRepository areaRepository;

    @Lazy
    @Autowired
    private IFmMerchantService self;


    // ================================================================
    // GET ALL MERCHANTS
    // ================================================================

    @Override
    public List<FmMerchant> getAllMerchants() {
        return merchantRepository.findAll();
    }


    // ================================================================
    // GET MERCHANT BY ID
    // ================================================================

    @Override
    public FmMerchantDto getMerchantById(Integer id) {

        FmMerchant merchant = merchantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Merchant ID " + id + " does not exist"));

        FmMerchantDto dto = FmMerchantMapper.mapToMerchantDto(merchant);

        log.info("[MERCHANT] Fetched by ID: merchantId={}, name={}", id, merchant.getMerchantName());

        return dto;
    }


    // ================================================================
    // COUNT MERCHANTS
    // ================================================================

    @Override
    public long countMerchants() {

        long count = merchantRepository.count();

        log.info("[MERCHANT] Count fetched: {}", count);

        return count;
    }


    // ================================================================
    // SINGLE MERCHANT CREATE
    //
    // IMPORTANT:
    // This method is intentionally NOT changed for bulk requirements.
    // ================================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FmMerchant createMerchant(FmMerchantRequestDTO dto) {
        return createMerchant(dto, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FmMerchant createMerchant(FmMerchantRequestDTO dto, MultipartFile aadharFile, MultipartFile panFile) {

        log.info("[MERCHANT] Creating merchant: email={}, phone={}", dto.getEmail(), dto.getPhone());

        validateUniqueness(dto.getEmail(), dto.getPhone(), dto.getPan(), dto.getAdhar(), dto.getAccountNumber());

        FmMerchant merchant = FmMerchantMapper.toEntity(dto);

        merchant = merchantRepository.save(merchant);

        log.info("[MERCHANT] Saved: merchantId={}, name={}", merchant.getMerchantId(), merchant.getMerchantName());

        /*
         * Existing approval flow.
         */
        approvalRequestService.createApprovalRequest(FmAppConstants.TYPE_MERCHANT, merchant.getMerchantId(), merchant.getMerchantId());

        saveKyc(dto, merchant, aadharFile, panFile);

        saveBankDetails(dto, merchant.getMerchantId());

        createMerchantUser(dto, merchant.getMerchantId());

        log.info("[MERCHANT] Onboarding complete: merchantId={}", merchant.getMerchantId());

        /*
         * Existing single merchant email flow.
         */
        emailService.sendMerchantRegistrationEmail(merchant.getMerchantEmail(), merchant.getMerchantName());

        log.info("[MERCHANT] Registration email triggered: merchantId={}, email={}", merchant.getMerchantId(), merchant.getMerchantEmail());

        return merchant;
    }


    // ================================================================
    // BULK-ONLY MERCHANT CREATE
    // ================================================================

    /**
     * Creates a merchant from BULK upload only.
     * <p>
     * IMPORTANT:
     * <p>
     * 1. Email duplicate validation is skipped.
     * 2. Email can be blank.
     * 3. Username can be blank.
     * 4. Password can be blank.
     * 5. Username is generated when blank.
     * 6. Password is generated when blank.
     * 7. Address is saved using a separate method.
     * 8. State/City/Area names are converted into IDs.
     * 9. Approval flow is retained.
     * 10. KYC flow is retained.
     * 11. Bank flow is retained.
     * 12. Normal createMerchant() is not changed.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FmMerchant createMerchantBulkUpload(FmMerchantRequestDTO dto) {

        log.info("[BULK] Creating merchant: email={}, phone={}", dto.getEmail(), dto.getPhone());

        // ============================================================
        // BULK DUPLICATE CHECKS
        //
        // EMAIL IS INTENTIONALLY NOT CHECKED.
        // ============================================================

        if (!isBlank(dto.getPhone()) && merchantRepository.existsByMerchantPhone(dto.getPhone())) {

            throw new MerchantAlreadyExistsException("Phone already registered: " + dto.getPhone());
        }

        if (!isBlank(dto.getPan()) && userKycRepository.existsByPanNumber(dto.getPan())) {

            throw new MerchantAlreadyExistsException("PAN already registered: " + dto.getPan());
        }

        if (!isBlank(dto.getAdhar()) && userKycRepository.existsByAadhaarNumber(dto.getAdhar())) {

            throw new MerchantAlreadyExistsException("Aadhaar already registered: " + dto.getAdhar());
        }

//        if (!isBlank(dto.getFssai()) && userKycRepository.existsByFssaiNumber(dto.getFssai())) {
//
//            throw new MerchantAlreadyExistsException("FSSAI already registered: " + dto.getFssai());
//        }

        if (!isBlank(dto.getAccountNumber()) && bankDetailsRepository.existsByAccountNumber(dto.getAccountNumber())) {

            throw new MerchantAlreadyExistsException("Account number already registered: " + dto.getAccountNumber());
        }


        // ============================================================
        // CREATE MERCHANT
        // ============================================================

        FmMerchant merchant = FmMerchantMapper.toEntity(dto);

        // ============================================================
        // BULK EMAIL HANDLING
        // ============================================================
        // Email is optional only for BULK upload.
        // Never persist an empty string ("") because merchant_email
        // has a UNIQUE constraint in PostgreSQL.
        //
        // Blank email  -> NULL
        // Provided email -> keep the supplied value
        // ============================================================
        if (isBlank(dto.getEmail())) {
            merchant.setMerchantEmail(null);
            log.info("[BULK] Blank email converted to NULL for merchant before save");
        } else {
            merchant.setMerchantEmail(dto.getEmail().trim());
        }

        merchant = merchantRepository.save(merchant);

        log.info("[BULK] Saved: merchantId={}, name={}", merchant.getMerchantId(), merchant.getMerchantName());


        // ============================================================
        // APPROVAL
        // ============================================================

        approvalRequestService.createApprovalRequest(FmAppConstants.TYPE_MERCHANT, merchant.getMerchantId(), merchant.getMerchantId());


        // ============================================================
        // KYC
        // ============================================================

        saveKyc(dto, merchant);


        // ============================================================
        // BANK
        // ============================================================

        saveBankDetails(dto, merchant.getMerchantId());


        // ============================================================
        // MERCHANT ADDRESS
        //
        // BULK ONLY
        //
        // State name -> state ID
        // City name  -> city ID
        // Area name  -> area ID
        // ============================================================

        saveMerchantAddress(dto, merchant.getMerchantId());


        // ============================================================
        // MERCHANT USER
        //
        // Bulk-specific method.
        //
        // Username:
        // provided -> use provided
        // blank    -> generate default
        //
        // Password:
        // provided -> use provided
        // blank    -> generate default
        // ============================================================

        createMerchantBulkUser(dto, merchant.getMerchantId());


        // ============================================================
        // EMAIL
        //
        // Intentionally skipped for BULK.
        // ============================================================

        log.info("[BULK] Merchant registration email skipped: merchantId={}", merchant.getMerchantId());

        return merchant;
    }


    // ================================================================
    // BULK UPLOAD
    // ================================================================

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


        int success = 0;
        int failure = 0;

        List<FmBulkUploadResultDTO.RowErrorDTO> errors = new ArrayList<>();


        // ============================================================
        // PROCESS EACH ROW
        // ============================================================

        for (int i = 0; i < rows.size(); i++) {

            FmMerchantRequestDTO dto = rows.get(i);

            int rowNum = i + 2;

            try {

                /*
                 * ====================================================
                 * BULK CREDENTIAL PREPARATION
                 *
                 * Username and password are optional in CSV.
                 *
                 * We do NOT generate them here because merchant ID
                 * is required for the default username.
                 *
                 * Generation happens in createMerchantBulkUser()
                 * after merchant is saved.
                 * ====================================================
                 */

                if (isBlank(dto.getEmail())) {

                    log.info("[BULK] Email not provided for row {}. " + "Email validation will be skipped.", rowNum);
                }

                if (isBlank(dto.getUsername())) {

                    log.info("[BULK] Username not provided for row {}. " + "Default username will be generated.", rowNum);
                }

                if (isBlank(dto.getPassword())) {

                    log.info("[BULK] Password not provided for row {}. " + "Default password will be generated.", rowNum);
                }


                // ====================================================
                // VALIDATE DTO
                //
                // IMPORTANT:
                //
                // Email/username/password are handled specially for
                // BULK.
                //
                // Other validation remains active.
                // ====================================================

                Set<ConstraintViolation<FmMerchantRequestDTO>> violations = validator.validate(dto);


                boolean hasNonEmailValidationError = false;


                for (ConstraintViolation<FmMerchantRequestDTO> violation : violations) {

                    String field = violation.getPropertyPath().toString();


                    // =================================================
                    // EMAIL
                    //
                    // Email validation is ignored ONLY for bulk.
                    // =================================================

                    if ("email".equalsIgnoreCase(field)) {

                        log.info("[BULK] Email validation skipped " + "for row {}. value={}", rowNum, dto.getEmail());

                        continue;
                    }


                    // =================================================
                    // USERNAME
                    //
                    // Blank username is allowed in bulk.
                    //
                    // If a username was supplied and invalid, keep
                    // the validation error.
                    // =================================================

                    if ("username".equalsIgnoreCase(field) && isBlank(dto.getUsername())) {

                        log.info("[BULK] Username validation skipped " + "for row {}. " + "Default username will be generated.", rowNum);

                        continue;
                    }


                    // =================================================
                    // PASSWORD
                    //
                    // Blank password is allowed in bulk.
                    //
                    // If password is supplied and invalid, keep
                    // validation error.
                    // =================================================

                    if ("password".equalsIgnoreCase(field) && isBlank(dto.getPassword())) {

                        log.info("[BULK] Password validation skipped " + "for row {}. " + "Default password will be generated.", rowNum);

                        continue;
                    }


                    // =================================================
                    // OTHER VALIDATION ERROR
                    // =================================================

                    errors.add(FmBulkUploadResultDTO.RowErrorDTO.builder().rowNumber(rowNum).field(field).value(violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : "").reason(violation.getMessage()).build());

                    hasNonEmailValidationError = true;
                }


                // ====================================================
                // STOP ROW IF VALIDATION FAILED
                // ====================================================

                if (hasNonEmailValidationError) {

                    failure++;

                    continue;
                }


                // ====================================================
                // CREATE BULK MERCHANT
                //
                // Dedicated method ensures normal merchant creation
                // is not affected.
                // ====================================================

                FmMerchant merchant = self.createMerchantBulkUpload(dto);

                success++;

                log.info("[BULK] Row {} saved: merchantId={}", rowNum, merchant.getMerchantId());


            } catch (MerchantAlreadyExistsException e) {

                log.warn("[BULK] Row {} skipped (duplicate): {}", rowNum, e.getMessage());

                errors.add(FmBulkUploadResultDTO.RowErrorDTO.builder().rowNumber(rowNum).field("duplicate").value("").reason(e.getMessage()).build());

                failure++;


            } catch (Exception e) {

                log.error("[BULK] Row {} failed: {}", rowNum, e.getMessage(), e);

                errors.add(FmBulkUploadResultDTO.RowErrorDTO.builder().rowNumber(rowNum).field("unknown").value("").reason(e.getMessage() != null ? e.getMessage() : "Unknown error").build());

                failure++;
            }
        }


        log.info("[BULK] Complete: total={}, success={}, failure={}", rows.size(), success, failure);


        return FmBulkUploadResultDTO.builder().totalRows(rows.size()).successCount(success).failureCount(failure).errors(errors).build();
    }


    // ================================================================
    // MERCHANT UNIQUENESS
    //
    // USED BY NORMAL SINGLE MERCHANT FLOW.
    // DO NOT CHANGE FOR BULK.
    // ================================================================

    private void validateUniqueness(String email, String phone, String pan, String aadhaar, String accountNumber) {

        if (merchantRepository.existsByMerchantEmail(email)) {

            throw new MerchantAlreadyExistsException("Email already registered: " + email);
        }

        if (merchantRepository.existsByMerchantPhone(phone)) {

            throw new MerchantAlreadyExistsException("Phone already registered: " + phone);
        }

        if (pan != null && userKycRepository.existsByPanNumber(pan)) {

            throw new MerchantAlreadyExistsException("PAN already registered: " + pan);
        }

        if (aadhaar != null && userKycRepository.existsByAadhaarNumber(aadhaar)) {

            throw new MerchantAlreadyExistsException("Aadhaar already registered: " + aadhaar);
        }

//        if (fssai != null && userKycRepository.existsByFssaiNumber(fssai)) {
//
//            throw new MerchantAlreadyExistsException("FSSAI already registered: " + fssai);
//        }

        if (accountNumber != null && !accountNumber.isBlank() && bankDetailsRepository.existsByAccountNumber(accountNumber)) {

            throw new MerchantAlreadyExistsException("Account number already registered: " + accountNumber);
        }
    }


    // ================================================================
    // SAVE KYC
    // ================================================================

    private void saveKyc(FmMerchantRequestDTO dto, FmMerchant merchant) {
        saveKyc(dto, merchant, null, null);
    }

    private void saveKyc(FmMerchantRequestDTO dto, FmMerchant merchant,
                         MultipartFile aadharFile, MultipartFile panFile) {

        FmUserKyc kyc = FmMerchantMapper.toKycEntity(dto, merchant);

        if (aadharFile != null && !aadharFile.isEmpty()) {
            kyc.setAadhaarNumberUrl(s3Service.uploadKycDocument(
                    aadharFile, FmAppConstants.TYPE_MERCHANT, merchant.getMerchantId(), "aadhar"));
        }
        if (panFile != null && !panFile.isEmpty()) {
            kyc.setPanNumberUrl(s3Service.uploadKycDocument(
                    panFile, FmAppConstants.TYPE_MERCHANT, merchant.getMerchantId(), "pan"));
        }
        userKycRepository.save(kyc);

        log.info("[KYC] Saved for merchantId={}", merchant.getMerchantId());
    }


    // ================================================================
    // SAVE BANK DETAILS
    // ================================================================

    private void saveBankDetails(FmMerchantRequestDTO dto, Integer merchantId) {

        boolean hasBankData = !isBlank(dto.getAccountNumber()) || !isBlank(dto.getIfscCode());

        if (!hasBankData) {

            log.info("[BANK] No bank details supplied " + "for merchantId={}", merchantId);

            return;
        }


        FmMerchantBankDetails bank = FmMerchantMapper.toBankEntity(dto, merchantId);

        bankDetailsRepository.save(bank);

        log.info("[BANK] Details saved for merchantId={}", merchantId);
    }


    // ================================================================
    // SAVE MERCHANT ADDRESS
    //
    // REUSABLE METHOD
    //
    // BULK CSV/EXCEL:
    //
    // State Name -> stateRepository -> stateId
    // City Name  -> cityRepository  -> cityId
    // Area Name  -> areaRepository  -> areaId
    //
    // Database:
    //
    // jippy_address_id = merchantId
    // address_type     = MERCHANT
    // ================================================================

    private void saveMerchantAddress(FmMerchantRequestDTO dto, Integer merchantId) {

        log.info("[ADDRESS][BULK] Saving merchant address " + "for merchantId={}", merchantId);


        // ============================================================
        // CHECK ADDRESS DATA
        // ============================================================

        boolean hasAddressData = !isBlank(dto.getBuildingNumber()) || !isBlank(dto.getRoad()) || !isBlank(dto.getLandmark()) || !isBlank(dto.getStateName()) || !isBlank(dto.getCityName()) || !isBlank(dto.getAreaName());


        /*
         * If no address values were supplied at all,
         * don't create an invalid address record.
         */
        if (!hasAddressData) {

            log.info("[ADDRESS][BULK] No address data supplied " + "for merchantId={}. Address not created.", merchantId);

            return;
        }


        // ============================================================
        // REQUIRED ADDRESS LOOKUPS
        // ============================================================

        if (isBlank(dto.getStateName())) {

            throw new ResourceNotFoundException("State name is required for merchant address");
        }

        if (isBlank(dto.getCityName())) {

            throw new ResourceNotFoundException("City name is required for merchant address");
        }

        if (isBlank(dto.getAreaName())) {

            throw new ResourceNotFoundException("Area name is required for merchant address");
        }


        // ============================================================
        // STATE
        //
        // CSV:
        //
        // Telangana
        //
        // DB:
        //
        // state_id = 36
        // ============================================================

        FmState state = stateRepository.findByStateNameIgnoreCase(dto.getStateName().trim()).orElseThrow(() -> new ResourceNotFoundException("State not found: " + dto.getStateName()));

        Integer stateId = state.getStateId();


        log.info("[ADDRESS][BULK] State resolved: " + "name={}, stateId={}", dto.getStateName(), stateId);


        // ============================================================
        // CITY
        //
        // CSV:
        //
        // Hyderabad
        //
        // DB:
        //
        // city_id = ...
        // ============================================================

        FmCity city = cityRepository.findByCityNameIgnoreCase(dto.getCityName().trim()).orElseThrow(() -> new ResourceNotFoundException("City not found: " + dto.getCityName()));


        Integer cityId = city.getCityId();


        /*
         * IMPORTANT:
         *
         * Make sure the city belongs to the selected state.
         *
         * This prevents:
         *
         * State = Telangana
         * City  = Bengaluru
         *
         * from being accidentally accepted.
         */
        if (city.getStateId() != null && !city.getStateId().equals(stateId)) {

            throw new ResourceNotFoundException("City '" + dto.getCityName() + "' does not belong to state '" + dto.getStateName() + "'");
        }


        log.info("[ADDRESS][BULK] City resolved: " + "name={}, cityId={}, stateId={}", dto.getCityName(), cityId, stateId);


        // ============================================================
        // AREA
        //
        // Area is resolved using:
        //
        // area name + city ID
        // ============================================================

        FmArea area = areaRepository.findByAreaNameIgnoreCaseAndCityId(dto.getAreaName().trim(), cityId).orElseThrow(() -> new ResourceNotFoundException("Area '" + dto.getAreaName() + "' not found for city '" + dto.getCityName() + "'"));


        Integer areaId = area.getAreaId();


        log.info("[ADDRESS][BULK] Area resolved: " + "name={}, areaId={}, cityId={}", dto.getAreaName(), areaId, cityId);


        // ============================================================
        // CHECK EXISTING MERCHANT ADDRESS
        // ============================================================

        Optional<FmAddress> existingAddress = addressRepository.findByJippyAddressIdAndAddressType(merchantId, "MERCHANT");


        FmAddress address;


        if (existingAddress.isPresent()) {

            /*
             * Normally this should not happen during initial merchant
             * creation because merchantId is newly generated.
             *
             * But updating the existing record is safer than creating
             * duplicate MERCHANT address records.
             */

            address = existingAddress.get();

            log.info("[ADDRESS][BULK] Existing MERCHANT address found. " + "Updating addressId={}, merchantId={}", address.getAddressId(), merchantId);

        } else {

            address = FmAddress.builder().addressId(generateNextAddressId()).jippyAddressId(merchantId).addressType("MERCHANT").createdAt(java.time.LocalDateTime.now()).build();

            log.info("[ADDRESS][BULK] Creating new MERCHANT address " + "for merchantId={}", merchantId);
        }


        // ============================================================
        // SET ADDRESS VALUES
        // ============================================================

        address.setJippyAddressId(merchantId);

        address.setAddressType("MERCHANT");

        address.setBuildingNumber(defaultAddressValue(dto.getBuildingNumber()));

        address.setRoad(defaultAddressValue(dto.getRoad()));

        address.setLandmark(defaultAddressValue(dto.getLandmark()));

        address.setStateId(stateId);

        address.setCityId(cityId);

        address.setAreaId(areaId);


        // ============================================================
        // UPDATE AUDIT FIELDS
        // ============================================================

        if (address.getCreatedAt() == null) {

            address.setCreatedAt(java.time.LocalDateTime.now());
        }


        // ============================================================
        // SAVE ADDRESS
        // ============================================================

        FmAddress savedAddress = addressRepository.save(address);


        log.info("[ADDRESS][BULK] Address saved successfully: " + "addressId={}, merchantId={}, " + "stateId={}, cityId={}, areaId={}, type={}", savedAddress.getAddressId(), merchantId, savedAddress.getStateId(), savedAddress.getCityId(), savedAddress.getAreaId(), savedAddress.getAddressType());
    }


    // ================================================================
    // GENERATE ADDRESS ID
    //
    // Your current FmAddress entity has:
    //
    // @Id
    // @Column(name = "address_id")
    // private Integer addressId;
    //
    // There is no @GeneratedValue currently.
    //
    // Therefore this method generates the next ID from existing
    // records.
    //
    // NOTE:
    // For high-concurrency production environments, PostgreSQL
    // sequence/identity is recommended.
    // ================================================================

    private Integer generateNextAddressId() {

        return addressRepository.findAll().stream().map(FmAddress::getAddressId).filter(id -> id != null).max(Integer::compareTo).map(id -> id + 1).orElse(1);
    }


    // ================================================================
    // DEFAULT ADDRESS VALUE
    //
    // Your address table has NOT NULL columns:
    //
    // building_number
    // road
    // landmark
    //
    // Therefore blank values cannot be inserted.
    // ================================================================

    private String defaultAddressValue(String value) {

        if (value == null || value.isBlank()) {

            return "N/A";
        }

        return value.trim();
    }


    // ================================================================
    // BULK MERCHANT USER
    //
    // Username:
    // supplied -> use supplied
    // blank    -> merchant_<merchantId>
    //
    // Password:
    // supplied -> use supplied
    // blank    -> secure generated password
    // ================================================================

    private void createMerchantBulkUser(FmMerchantRequestDTO dto, Integer merchantId) {


        // ============================================================
        // USERNAME
        // ============================================================

        String username = dto.getUsername();


        if (isBlank(username)) {

            username = generateUniqueMerchantUsername(merchantId);

            log.info("[BULK] Default username generated | " + "merchantId={} | username={}", merchantId, username);

        } else {

            username = username.trim();
        }


        // ============================================================
        // USERNAME DUPLICATE CHECK
        // ============================================================

        Optional<FmUser> existingUser = userRepository.findByUsernameAndUserType(username, FmAppConstants.TYPE_MERCHANT);


        if (existingUser.isPresent()) {

            throw new MerchantAlreadyExistsException("Username already exists: " + username);
        }


        // ============================================================
        // PASSWORD
        // ============================================================

        String password = dto.getPassword();


        if (isBlank(password)) {

            password = generateMerchantPassword();

            log.info("[BULK] Default password generated " + "for merchantId={}", merchantId);
        }


        // ============================================================
        // ENCODE PASSWORD
        // ============================================================

        String encodedPassword = passwordEncoder.encode(password);


        // ============================================================
        // CREATE USER
        // ============================================================

        FmUser user = FmMerchantMapper.toUserEntity(username, encodedPassword, merchantId, FmAppConstants.TYPE_MERCHANT);


        user = userRepository.save(user);


        // ============================================================
        // MERCHANT ROLE
        // ============================================================

        FmRoles role = roleRepository.findByRoleName(FmAppConstants.ROLE_MERCHANT);


        if (role == null) {

            throw new ResourceNotFoundException("Merchant role not found.");
        }


        // ============================================================
        // ROLE PERMISSIONS
        // ============================================================

        List<FmRolePermissions> rolePermissions = rolePermissionsRepository.findByRole(role);


        if (rolePermissions.isEmpty()) {

            throw new ResourceNotFoundException("No permissions mapped to Merchant role.");
        }


        for (FmRolePermissions permission : rolePermissions) {

            FmUserRolePermissions userRole = FmMerchantMapper.toUserRolesEntity(user, permission);

            userRolesRepository.save(userRole);
        }


        log.info("[BULK] Merchant login created successfully | " + "merchantId={} | username={}", merchantId, username);
    }


    // ================================================================
    // GENERATE UNIQUE DEFAULT USERNAME
    // ================================================================

    private String generateUniqueMerchantUsername(Integer merchantId) {

        String baseUsername = "merchant_" + merchantId;


        String username = baseUsername;

        int suffix = 1;


        while (userRepository.findByUsernameAndUserType(username, FmAppConstants.TYPE_MERCHANT).isPresent()) {

            username = baseUsername + "_" + suffix++;


            if (username.length() > 50) {

                username = baseUsername.substring(0, Math.min(baseUsername.length(), 45)) + "_" + suffix;
            }
        }


        return username;
    }


    // ================================================================
    // GENERATE SECURE DEFAULT PASSWORD
    // ================================================================

    private String generateMerchantPassword() {

        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        String lower = "abcdefghijklmnopqrstuvwxyz";

        String numbers = "0123456789";

        String special = "@#$%&!";

        String all = upper + lower + numbers + special;


        SecureRandom random = new SecureRandom();


        StringBuilder password = new StringBuilder(12);


        // Guarantee uppercase
        password.append(upper.charAt(random.nextInt(upper.length())));


        // Guarantee lowercase
        password.append(lower.charAt(random.nextInt(lower.length())));


        // Guarantee number
        password.append(numbers.charAt(random.nextInt(numbers.length())));


        // Guarantee special character
        password.append(special.charAt(random.nextInt(special.length())));


        // Remaining characters
        while (password.length() < 12) {

            password.append(all.charAt(random.nextInt(all.length())));
        }


        // Shuffle password
        for (int i = password.length() - 1; i > 0; i--) {

            int j = random.nextInt(i + 1);

            char temp = password.charAt(i);

            password.setCharAt(i, password.charAt(j));

            password.setCharAt(j, temp);
        }


        return password.toString();
    }


    // ================================================================
    // COMMON BLANK CHECK
    // ================================================================

    private boolean isBlank(String value) {

        return value == null || value.isBlank();
    }


    // ================================================================
    // NORMAL SINGLE MERCHANT USER
    //
    // IMPORTANT:
    // This is your existing single merchant user flow.
    // It is NOT used by bulk upload.
    // ================================================================

    private void createMerchantUser(FmMerchantRequestDTO dto, Integer merchantId) {

        String username = dto.getUsername().trim();


        Optional<FmUser> existingUser = userRepository.findByUsernameAndUserType(username, FmAppConstants.TYPE_MERCHANT);


        if (existingUser.isPresent()) {

            throw new MerchantAlreadyExistsException("Username already exists.");
        }


        String encodedPassword = passwordEncoder.encode(dto.getPassword());


        FmUser user = FmMerchantMapper.toUserEntity(username, encodedPassword, merchantId, FmAppConstants.TYPE_MERCHANT);


        user = userRepository.save(user);


        FmRoles role = roleRepository.findByRoleName(FmAppConstants.ROLE_MERCHANT);


        if (role == null) {

            throw new ResourceNotFoundException("Merchant role not found.");
        }


        List<FmRolePermissions> rolePermissions = rolePermissionsRepository.findByRole(role);


        if (rolePermissions.isEmpty()) {

            throw new ResourceNotFoundException("No permissions mapped to Merchant role.");
        }


        for (FmRolePermissions permission : rolePermissions) {

            FmUserRolePermissions userRole = FmMerchantMapper.toUserRolesEntity(user, permission);

            userRolesRepository.save(userRole);
        }


        log.info("Merchant login created successfully. username={}", username);
    }


    // ================================================================
    // BULK ERROR RESULT
    // ================================================================

    private FmBulkUploadResultDTO buildErrorResult(int rowNum, String reason) {

        return FmBulkUploadResultDTO.builder().totalRows(0).successCount(0).failureCount(0).errors(List.of(FmBulkUploadResultDTO.RowErrorDTO.builder().rowNumber(rowNum).field("file").value("").reason(reason).build())).build();
    }


    // ================================================================
    // GET MERCHANT PROFILE
    // ================================================================

    public FmMerchantDto getMerchantProfile(int merchantId) {

        log.info("Fetching merchant profile for merchantId: {}", merchantId);


        FmMerchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> {

            log.error("Merchant not found with id: {}", merchantId);

            return new ResourceNotFoundException("Merchant not found with id: " + merchantId);
        });


        log.info("Merchant fetched successfully for merchantId: {}", merchantId);


        return FmMerchantMapper.mapToMerchantDto(merchant);
    }


    // ================================================================
    // GET MERCHANT + BANK
    // ================================================================

    @Override
    public FmMerchantWithBankDto getMerchantWithBank(Integer merchantId) {

        log.info("Fetching merchant with bank details " + "for merchantId: {}", merchantId);


        FmMerchantWithBankProjection data = merchantRepository.getMerchantWithBank(merchantId);


        if (data == null) {

            log.error("Merchant with bank details not found " + "for merchantId: {}", merchantId);

            throw new ResourceNotFoundException("Merchant not found with :" + merchantId);
        }


        log.info("Successfully fetched merchant + bank details " + "for merchantId: {}", merchantId);

        FmMerchantWithBankDto response = FmMerchantMapper.mapToMerchantWithBankDto(data);

        FmUserKyc kyc = userKycRepository.findByEntityIdAndEntityType(
                merchantId,
                FmAppConstants.TYPE_MERCHANT
        ).orElse(null);

        if (kyc != null) {
            response.setAadharNumber(kyc.getAadhaarNumber());
            response.setPanNumber(kyc.getPanNumber());
            response.setAadhaarNumberUrl(kyc.getAadhaarNumberUrl());
            response.setPanNumberUrl(kyc.getPanNumberUrl());
        }

        return response;
    }


    // ================================================================
    // UPDATE MERCHANT + BANK
    // ================================================================

    @Override
    @Transactional
    public FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto) {
        return updateMerchantProfile(dto, null, null);
    }

    @Override
    @Transactional
    public FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto,
                                                        MultipartFile aadharFile, MultipartFile panFile) {

        log.info("Updating merchant profile for merchantId: {}", dto.getMerchantId());

        log.debug("Request DTO: {}", dto);


        // ============================================================
        // FETCH MERCHANT
        // ============================================================

        FmMerchant merchant = merchantRepository.findById(dto.getMerchantId().intValue()).orElseThrow(() -> {

            log.error("Merchant not found with ID: {}", dto.getMerchantId());

            return new ResourceNotFoundException("Merchant not found with ID :" + dto.getMerchantId());
        });


        // ============================================================
        // DUPLICATE EMAIL CHECK
        // ============================================================

        if (!merchant.getMerchantEmail().equalsIgnoreCase(dto.getMerchantEmail()) && merchantRepository.existsByMerchantEmail(dto.getMerchantEmail())) {

            throw new DuplicateResourceException("Merchant email already exists");
        }


        // ============================================================
        // UPDATE MERCHANT
        // ============================================================

        FmMerchantMapper.updateMerchantEntity(merchant, dto);


        merchantRepository.save(merchant);


        log.info("Merchant updated successfully for merchantId: {}", dto.getMerchantId());

        FmUserKyc kyc = userKycRepository.findByEntityIdAndEntityType(
                dto.getMerchantId(),
                FmAppConstants.TYPE_MERCHANT
        ).orElseGet(FmUserKyc::new);

        kyc.setEntityId(dto.getMerchantId());
        kyc.setEntityType(FmAppConstants.TYPE_MERCHANT);

        FmMerchantMapper.updateMerchantKycEntity(kyc, dto);
        if (aadharFile != null && !aadharFile.isEmpty()) {
            kyc.setAadhaarNumberUrl(s3Service.replaceKycDocument(
                    aadharFile, FmAppConstants.TYPE_MERCHANT, dto.getMerchantId(), "aadhar",
                    kyc.getAadhaarNumberUrl()));
        }
        if (panFile != null && !panFile.isEmpty()) {
            kyc.setPanNumberUrl(s3Service.replaceKycDocument(
                    panFile, FmAppConstants.TYPE_MERCHANT, dto.getMerchantId(), "pan",
                    kyc.getPanNumberUrl()));
        }
        userKycRepository.save(kyc);

        log.info("Merchant KYC updated successfully for merchantId: {}", dto.getMerchantId());


        // ============================================================
        // FETCH BANK
        // ============================================================

        FmMerchantBankDetails bank = bankDetailsRepository.findByRecipientIdAndUserType(dto.getMerchantId(), "MERCHANT").orElseThrow(() -> new ResourceNotFoundException("Bank details not found"));


        // ============================================================
        // DUPLICATE ACCOUNT CHECK
        // ============================================================

        if (!bank.getAccountNumber().equals(dto.getAccountNumber()) && bankDetailsRepository.existsByAccountNumber(dto.getAccountNumber())) {

            throw new DuplicateResourceException("Account number already exists");
        }


        // ============================================================
        // UPDATE BANK
        // ============================================================

        FmMerchantMapper.updateBankEntity(bank, dto);


        bankDetailsRepository.save(bank);


        log.info("Bank details updated successfully " + "for merchantId: {}", dto.getMerchantId());


        // ============================================================
        // RETURN UPDATED DATA
        // ============================================================

        FmMerchantWithBankDto response = getMerchantWithBank(dto.getMerchantId());


        log.info("Returning updated merchant + bank response " + "for merchantId: {}", dto.getMerchantId());


        return response;
    }


    // ================================================================
    // UPDATE MERCHANT PROFILE PICTURE
    // ================================================================

    @Override
    public FmResponseDto updateMerchantProfilePic(FmMerchantDto merchantDto) {

        log.info("Updating merchant profile picture " + "for merchantId: {}", merchantDto.getMerchantId());


        FmMerchant merchant = merchantRepository.findById(merchantDto.getMerchantId()).orElseThrow(() -> {

            log.error("Merchant not found with ID: {}", merchantDto.getMerchantId());

            return new ResourceNotFoundException("Merchant not found with ID :" + merchantDto.getMerchantId());
        });


        merchant.setProfilePicUrl(merchantDto.getProfilePicUrl());


        merchantRepository.save(merchant);


        log.info("Merchant profile picture updated successfully " + "for merchantId: {}", merchantDto.getMerchantId());


        return new FmResponseDto("200", "Profile picture url: " + merchantDto.getProfilePicUrl());
    }
}
