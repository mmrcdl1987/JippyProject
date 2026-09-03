package com.jippy.foodandmart.serviceImpl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.division.dto.FmNearbyOutletDto;
import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.exception.BadRequestException;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DivisionFeignClient;
import com.jippy.foodandmart.mapper.FmMerchantMapper;
import com.jippy.foodandmart.mapper.FmOutletMapper;
import com.jippy.foodandmart.mapper.FmPublicOutletMapper;
import com.jippy.foodandmart.projections.*;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service implementation for outlet management.
 *
 * <p>Layer order: Controller → IOutletService → OutletServiceImpl → OutletMapper → Repository → Entity.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmOutletServiceImpl implements IFmOutletService {
    private static final GeometryFactory GEO_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private final FmOutletRepository outletRepository;
    private final FmOutletAddressRepository addressRepository;
    private final FmOutletDayRepository dayRepository;
    private final FmMerchantRepository merchantRepository;
    private final FmUserRepository userRepository;
    private final FmStateRepository stateRepository;
    private final FmAreaRepository areaRepository;
    private final FmRoleRepository roleRepository;
    private final FmUserRolesRepository userRolesRepository;
    private final FmRolePermissionsRepository rolePermissionsRepository;
    private final FmProductAvailableTimingRepository productAvailableTimingRepository;
    private final FmProductRepository productRepository;
    private final FmCategoryRepository categoryRepository;
    private final FmProductVariantRepository productVariantRepository;
    private final FmGoogleMapsService googleMapsService;
    private final PasswordEncoder passwordEncoder;
    // private final FmEmailOtpVerificationRepository otpRepository;
    private final FmFavoriteOutletRepository favoriteOutletRepository;
    private final FmMerchantBankDetailsRepository merchantBankDetailsRepository;
    private final FmOutletDayRepository outletDayRepository;
    private final FmCityRepository cityRepository;
    private final FmCuisineTypeRepository cuisineTypeRepository;
    private final FmUserKycRepository userKycRepository;
    private final DivisionFeignClient divisionFeignClient;
    private final PromotionPlanRepository promotionPlanRepository;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final MealTypeTimingRepository mealTypeTimingRepository;
    private final S3Service s3Service;
    private final EmailService emailService;
    private final CacheInvalidateServiceImpl cacheInvalidateService;
    private final IFmApprovalRequestService approvalRequestService;

    @Override
    @Transactional
    public FmOutletCreateResponseDTO createOutlet(FmOutletRequestDTO dto) {

        log.info("Creating new outlet : {}", dto.getOutletName());

        /*
         * Check Merchant Exists.
         */
        FmMerchant merchant = merchantRepository.findById(dto.getMerchantId()).orElseThrow(() -> {

            log.error("Merchant not found : {}", dto.getMerchantId());

            return new ResourceNotFoundException("Merchant not found with id : " + dto.getMerchantId());
        });

        /*
         * Check Outlet Phone.
         */
        if (outletRepository.existsByOutletPhone(dto.getOutletPhone())) {

            throw new IllegalArgumentException("Outlet phone No already exists.");
        }

        /*
         * Check Username.
         */
        if (userRepository.findByUsernameAndUserType(dto.getUsername(), FmAppConstants.TYPE_OUTLET).isPresent()) {

            throw new IllegalArgumentException("Username already exists.");
        }

        /*
         * Check whether the merchant already has the same outlet
         * in the selected area.
         *
         * Same Merchant + Same Outlet Name + Same Area -> Not Allowed
         * Same Merchant + Same Outlet Name + Different Area -> Allowed
         */
        if (outletRepository.existsByMerchantAndOutletNameAndArea(dto.getMerchantId(), dto.getOutletName(), dto.getAreaId())) {

            log.error("Outlet '{}' already exists for merchantId {} in areaId {}", dto.getOutletName(), dto.getMerchantId(), dto.getAreaId());

            throw new IllegalArgumentException("Outlet already exists for this merchant in the selected area.");
        }

        /*
         * Convert DTO to Entity.
         */
        FmOutlet outlet = FmOutletMapper.toEntity(dto);

        /*
         * Outlet email should use the merchant email.
         */
        outlet.setOutletEmail(merchant.getMerchantEmail());

        /*
         * Save Outlet Location.
         */
        outlet.setOutletLocation(buildPoint(dto.getLatitude(), dto.getLongitude()));

        /*
         * Save Outlet.
         */
        outlet = outletRepository.save(outlet);

        log.info("Outlet saved successfully with outletId : {}", outlet.getOutletId());

        /*
         * Save Outlet KYC Details.
         */
        saveOutletKyc(dto, outlet.getOutletId());

        /**
         * Create Approval Request for the newly created Outlet.
         *
         * Every new Outlet enters the approval workflow
         * at Level 1 with PENDING status.
         */
        approvalRequestService.createApprovalRequest(FmAppConstants.TYPE_OUTLET, outlet.getOutletId(), outlet.getOutletId());

        /*
         * Save Address.
         */
        saveAddressUsingIds(dto, outlet.getOutletId());

        /*
         * Save Operating Days.
         */
        saveOperatingDays(dto, outlet.getOutletId());

        /*
         * Save Login User.
         */
        saveOutletUser(dto.getUsername(), dto.getPassword(), outlet.getOutletId());

        /*
         * Save Bank Details.
         */
        saveOutletBankDetails(dto, outlet.getOutletId());

        log.info("Outlet '{}' created successfully for merchantId {}", outlet.getOutletName(), outlet.getMerchantId());

        /*
         * Email #3:
         * Outlet Registration Successful
         */
        emailService.sendOutletRegistrationEmail(outlet.getOutletEmail(), outlet.getOutletName(), merchant.getMerchantName());

        /*
         * Create Response DTO.
         */
        FmOutletCreateResponseDTO createOutlet = FmOutletMapper.toCreateResponseDto(dto, outlet);

        return createOutlet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadOrUpdateOutletImage(Integer outletId, MultipartFile image) {

        log.info("Updating outlet image. outletId={}", outletId);

        // 1. Validate outlet
        FmOutlet outlet = outletRepository.findById(outletId).orElseThrow(() -> {

            log.warn("Outlet not found. outletId={}", outletId);

            return new ResourceNotFoundException("Outlet not found with id: " + outletId);
        });

        // 2. Validate image
        if (image == null || image.isEmpty()) {

            throw new BadRequestException("Outlet image is required.");
        }

        // 3. Keep old S3 URL
        String oldImageUrl = outlet.getOutletPicUrl();

        // 4. Get merchant ID for S3 path
        Integer merchantId = outlet.getMerchantId();

        // 5. Upload new image to S3
        String newImageUrl = s3Service.uploadOutletImage(image, merchantId);

        try {

            // 6. Update outlet image URL
            outlet.setOutletPicUrl(newImageUrl);

            outletRepository.save(outlet);

            log.info("Outlet image URL updated successfully. outletId={}, newImageUrl={}", outletId, newImageUrl);

            // 7. Delete old image after DB update
            if (StringUtils.hasText(oldImageUrl) && !oldImageUrl.equals(newImageUrl)) {

                s3Service.deleteFile(oldImageUrl);

                log.info("Old outlet image deleted successfully. outletId={}", outletId);
            }

            return newImageUrl;

        } catch (Exception ex) {

            log.error("Failed to update outlet image. outletId={}", outletId, ex);

            // DB update failed, remove newly uploaded S3 image
            s3Service.deleteFile(newImageUrl);

            throw ex;
        }
    }

    //---------------------------------------------------------------------------------------
    @Override
    @Transactional
    public FmUpdateOutletRequestDTO updateOutletDetailsByMerchant(Integer outletId, FmUpdateOutletRequestDTO dto) {

        log.info("Updating outlet details for outletId : {}", outletId);

        /*
         * Validate Outlet.
         */
        FmOutlet outlet = outletRepository.findById(outletId).orElseThrow(() -> new ResourceNotFoundException("Outlet not found with id : " + outletId));

        /*
         * Validate Merchant.
         */
        merchantRepository.findById(dto.getMerchantId()).orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id : " + "" + dto.getMerchantId()));

        /*  comment during live
         * Validate State.
         */
        stateRepository.findById(dto.getStateId()).orElseThrow(() -> new ResourceNotFoundException("State not found with id : " + dto.getStateId()));

        /* comment during live
         * Validate City.
         */
        cityRepository.findById(dto.getCityId()).orElseThrow(() -> new ResourceNotFoundException("City not found with id : " + dto.getCityId()));

        /* comment during live
         * Validate Area.
         */
        areaRepository.findById(dto.getAreaId()).orElseThrow(() -> new ResourceNotFoundException("Area not found with id : " + dto.getAreaId()));

        /*
         * Validate Address.
         */
        FmOutletAddress address = addressRepository.findByJippyAddressIdAndAddressType(outletId, FmAppConstants.TYPE_OUTLET).orElseThrow(() -> new ResourceNotFoundException("Outlet address not found."));

        /*
         * Validate Bank Details.
         */
        FmMerchantBankDetails bankDetails = merchantBankDetailsRepository.findByRecipientIdAndUserType(outletId, FmAppConstants.TYPE_OUTLET).orElseThrow(() -> new ResourceNotFoundException("Outlet bank details not found."));

        /*
         * Validate duplicate account number.
         *
         * Same Merchant + Same Account Number      -> Allowed
         * Same Merchant + Different Account Number -> Allowed
         * Different Merchant + Same Account Number -> Not Allowed
         */
        if (merchantBankDetailsRepository.existsAccountNumberForAnotherMerchant(dto.getAccountNumber(), dto.getMerchantId())) {

            throw new DuplicateResourceException("Account number already belongs to another merchant.");
        }

        /*
         * Update Outlet.
         */
        FmOutletMapper.updateOutletEntity(outlet, dto);

        outletRepository.save(outlet);

        log.info("Outlet updated successfully.");

        /*
         * Update Address.
         */
        FmOutletMapper.updateOutletAddressEntity(address, dto);

        addressRepository.save(address);

        log.info("Outlet address updated successfully.");

        /*
         * Update Bank Details.
         */
        FmOutletMapper.updateOutletBankEntity(bankDetails, dto);

        merchantBankDetailsRepository.save(bankDetails);

        log.info("Outlet bank details updated successfully.");

        /*
         * Delete existing outlet operating days.
         */
        outletDayRepository.deleteByOutletId(outletId);

        /*
         * Save latest operating days.
         */
        List<FmOutletDay> outletDays = new ArrayList<>();

        for (FmOutletDayDTO dayDto : dto.getOperatingDays()) {

            outletDays.add(FmOutletMapper.toOutletDayEntity(dayDto, outletId));
        }
        outletDayRepository.saveAll(outletDays);

        log.info("Outlet operating days updated successfully.");

        // invalidate outlet details cache
        cacheInvalidateService.invalidateCache(outlet.getOutletId());

        /*
         * Return updated response.
         */
        return FmOutletMapper.toUpdateResponseDto(dto, outlet);
    }


    /**
     * Saves Outlet KYC Details.
     * <p>
     * Every newly created outlet stores its
     * FSSAI and GST details in user_kyc table.
     *
     * @param dto      Outlet Request DTO.
     * @param outletId Newly created Outlet Id.
     */
    private void saveOutletKyc(FmOutletRequestDTO dto, Integer outletId) {

        log.info("Saving Outlet KYC Details for Outlet Id: {}", outletId);

        FmUserKyc kyc = FmMerchantMapper.toOutletKycEntity(dto, outletId);

        userKycRepository.save(kyc);

        log.info("Outlet KYC Details saved successfully for Outlet Id: {}", outletId);
    }

    /**
     * Saves outlet bank details into user_bank_details table.
     * Every outlet should have its own bank account details.
     * These details are stored in user_bank_details table with user_type = OUTLET.
     */
    private void saveOutletBankDetails(FmOutletRequestDTO dto, Integer outletId) {

        log.info("Saving bank details for outletId : {}", outletId);

        /*
         * Check whether bank details already exist for this outlet.
         */
        Optional<FmMerchantBankDetails> bankDetails = merchantBankDetailsRepository.findByRecipientIdAndUserType(outletId, FmAppConstants.TYPE_OUTLET);

        if (bankDetails.isPresent()) {

            log.error("Bank details already exist for outletId : {}", outletId);

            throw new DuplicateResourceException("Bank details already exist for this outlet.");
        }

        /*
         * Validate duplicate account number.
         *
         * Same Merchant + Same Account Number      -> Allowed
         * Same Merchant + Different Account Number -> Allowed
         * Different Merchant + Same Account Number -> Not Allowed
         */
        if (merchantBankDetailsRepository.existsAccountNumberForAnotherMerchant(dto.getAccountNumber(), dto.getMerchantId())) {

            throw new DuplicateResourceException("Account number already belongs to another merchant.");
        }


        /*
         * Convert DTO to Entity.
         */
        FmMerchantBankDetails outletBankDetails = FmOutletMapper.toOutletBankEntity(dto, outletId);
        /*
         * Save bank details.
         */
        merchantBankDetailsRepository.save(outletBankDetails);
        log.info("Outlet bank details saved successfully.");
    }


    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public long countOutlets() {
        return outletRepository.count();
    }

    @Override
    public List<FmOutlet> getAllOutlets() {
        return outletRepository.findAll();
    }

    @Override
    public List<FmOutletSummaryDTO> getAllOutletsSummary() {
        List<FmOutlet> outlets = outletRepository.findAll();
        List<FmOutletSummaryDTO> result = new ArrayList<>();
        for (FmOutlet o : outlets) {
            FmOutletAddress addr = addressRepository.findByJippyAddressIdAndAddressType(o.getOutletId(), FmAppConstants.ADDRESS_TYPE_OUTLET).orElse(null);
            result.add(FmOutletSummaryDTO.from(o, 0, addr));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FmOutletSummaryDTO> getOutletsByMerchantId(Integer merchantId) {

        log.info("Fetching outlets for merchantId: {}", merchantId);

        if (merchantId == null || merchantId <= 0) {
            throw new IllegalArgumentException("merchantId must be greater than 0");
        }

        List<FmOutlet> outlets = outletRepository.findByMerchantId(merchantId);

        List<FmOutletSummaryDTO> result = new ArrayList<>();

        for (FmOutlet outlet : outlets) {

            FmOutletAddress address = addressRepository.findByJippyAddressId(outlet.getOutletId()).orElse(null);

            result.add(FmOutletSummaryDTO.from(outlet, 0, address));
        }

        log.info("Fetched {} outlets for merchantId={}", result.size(), merchantId);

        return result;
    }

    //    @Override
//    public FmOutlet getOutletById(Integer id) {
//        return outletRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException
//                        ("Outlet ID " + id + " does not exist"));
//    }
    @Override
    @Transactional(readOnly = true)
    public FmOutletResponseDto getOutletById(Integer outletId) {

        log.info("Fetching complete outlet details for outletId={}", outletId);

        // 1. Fetch outlet
        FmOutlet outlet = outletRepository.findById(outletId).orElseThrow(() -> {

            log.error("Outlet not found with outletId={}", outletId);

            return new ResourceNotFoundException("Outlet not found with id: " + outletId);
        });
        // 2. Basic outlet + location

        FmOutletResponseDto response = FmOutletMapper.toOutletResponseDto(outlet);
        // 3. Address
        FmOutletAddress address = addressRepository.findByJippyAddressIdAndAddressType(outletId, FmAppConstants.TYPE_OUTLET).orElse(null);

        FmOutletMapper.mapAddressToOutletResponse(response, address);
        // 4. Bank details
        FmMerchantBankDetails bankDetails = merchantBankDetailsRepository.findByRecipientIdAndUserType(outletId, FmAppConstants.TYPE_OUTLET).orElse(null);

        FmOutletMapper.mapBankDetailsToOutletResponse(response, bankDetails);
        // KYC Details - FSSAI + GST
        FmUserKyc kyc = userKycRepository.findByEntityIdAndEntityType(outletId, FmAppConstants.TYPE_OUTLET).orElse(null);

        FmOutletMapper.mapKycToOutletResponse(response, kyc);

        // 6. Operating days
        List<FmOutletDay> outletDays = dayRepository.findByOutletId(outletId);

        FmOutletMapper.mapOperatingDaysToOutletResponse(response, outletDays);
        // 7. Final log
        log.info("Successfully fetched complete outlet details for outletId={}", outletId);

        return response;
    }

    @Override
    public FmOutletCreatedDTO createOutletForBulkUploadAndOtpValidation(FmOutletRequestDTO dto) {
//    public FmOutletCreatedDTO createOutlet(FmOutletRequestDTO dto) {
        return null;
    }


//    ------------------------------------------------------------------------
//     ── Single Create ─────────────────────────────────────────────────────────
//     @Override
//     @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
//     public FmOutletCreatedDTO createOutlet(FmOutletRequestDTO dto) {
//
//         log.info("[OUTLET] Creating outlet: name={}, merchantId={}, phone={}",
//                 dto.getOutletName(),
//                 dto.getMerchantId(),
//                 dto.getOutletPhone());
//
//         validateOutletRequest(dto);
//
//         if (!merchantRepository.existsById(dto.getMerchantId())) {
//             throw new IllegalArgumentException(
//                     "Merchant ID " + dto.getMerchantId() + " does not exist");
//         }
//
//         if (outletRepository.existsByOutletPhone(dto.getOutletPhone())) {
//             throw new IllegalArgumentException(
//                     "An outlet with phone " + dto.getOutletPhone() + " already exists");
//         }
//
//         if (userRepository.findByUsernameAndUserType(
//                 dto.getUsername(),
//                 FmAppConstants.TYPE_OUTLET
//         ).isPresent()) {
//
//             throw new IllegalArgumentException(
//                     "Username already exists.");
//         }
//
//         if (outletRepository.existsByMerchantIdAndOutletName(
//                 dto.getMerchantId(),
//                 dto.getOutletName())) {
//
//             throw new IllegalArgumentException(
//                     "Outlet '" + dto.getOutletName() + "' already exists for this merchant");
//         }
//
//         Point location = buildPoint(
//                 dto.getLatitude(),
//                 dto.getLongitude());
//
//         FmMerchant merchant = merchantRepository.findById(dto.getMerchantId())
//                 .orElseThrow(() ->
//                         new ResourceNotFoundException("Merchant not found."));
//
//         // Merchant email and outlet email must be same
//         if (!merchant.getMerchantEmail().equalsIgnoreCase(dto.getEmail())) {
//
//             throw new IllegalArgumentException(
//                     "Merchant email and outlet email must be same."
//             );
//         }
//
//         // Verify latest CREATE_OUTLET OTP
//         FmEmailOtpVerification otpVerification =
//                 otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(
//                                 merchant.getMerchantEmail(),
//                                 FmOtpPurpose.CREATE_OUTLET)
//                         .orElseThrow(() ->
//                                 new InvalidOtpException(
//                                         "Please verify OTP before creating outlet."
//                                 ));
//
//         if (otpVerification.getStatus() != FmOtpStatus.VERIFIED
//                 || !Boolean.TRUE.equals(otpVerification.getIsVerified())) {
//
//             throw new InvalidOtpException(
//                     "Please verify OTP before creating outlet."
//             );
//         }
//
//
//         FmOutlet outlet = FmOutletMapper.toEntity(dto);
//
//         // Always use merchant email
//         outlet.setOutletEmail(
//                 merchant.getMerchantEmail()
//         );
//
//         outlet.setOutletLocation(location);
//
//         outlet = outletRepository.save(outlet);
//
//         log.info("[OUTLET] Saved: outletId={}", outlet.getOutletId());
//
//         saveAddress(dto, outlet.getOutletId());
//
//         saveOperatingDays(dto, outlet.getOutletId());
//
//         saveOutletUser(
//                 dto.getUsername(),
//                 dto.getPassword(),
//                 outlet.getOutletId()
//         );
//
//         otpVerification.setStatus(FmOtpStatus.CONSUMED);
//         otpVerification.setVerifiedAt(LocalDateTime.now());
//         otpRepository.save(otpVerification);
//
//         log.info("[OUTLET] Onboarding complete: outletId={}",
//                 outlet.getOutletId());
//
//         return FmOutletMapper.toCreatedDTO(outlet);
//     }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public FmOutletCreatedDTO createOutletBulkUpload(FmOutletRequestDTO dto) {
//
//        log.info("[OUTLET_BULK] Creating outlet: name={}, merchantId={}, phone={}", dto.getOutletName(), dto.getMerchantId(), dto.getOutletPhone());
//
//        /*
//         * Validate request.
//         */
//        validateOutletRequest(dto);
//
//        /*
//         * Check Merchant Exists.
//         */
//        FmMerchant merchant = merchantRepository.findById(dto.getMerchantId()).orElseThrow(() -> {
//
//            log.error("[OUTLET_BULK] Merchant not found: merchantId={}", dto.getMerchantId());
//
//            return new ResourceNotFoundException("Merchant not found with id: " + dto.getMerchantId());
//        });
//
//        /*
//         * Check Outlet Phone.
//         */
//        if (outletRepository.existsByOutletPhone(dto.getOutletPhone())) {
//
//            throw new IllegalArgumentException("An outlet with phone " + dto.getOutletPhone() + " already exists");
//        }
//
//        /*
//         * Check Username.
//         */
//        if (userRepository.findByUsernameAndUserType(dto.getUsername(), FmAppConstants.TYPE_OUTLET).isPresent()) {
//
//            throw new IllegalArgumentException("Username already exists.");
//        }
//
//        /*
//         * Check duplicate outlet.
//         */
//        if (outletRepository.existsByMerchantIdAndOutletName(dto.getMerchantId(), dto.getOutletName())) {
//
//            throw new IllegalArgumentException("Outlet '" + dto.getOutletName() + "' already exists for this merchant");
//        }
//
//        /*
//         * Build outlet location.
//         */
//        Point location = buildPoint(dto.getLatitude(), dto.getLongitude());
//
//        /*
//         * Merchant email and outlet email must be same.
//         */
//        if (!merchant.getMerchantEmail().equalsIgnoreCase(dto.getOutletEmail())) {
//
//            throw new IllegalArgumentException("Merchant email and outlet email must be same.");
//        }
//
//        /*
//         * Convert DTO to Entity.
//         */
//        FmOutlet outlet = FmOutletMapper.toEntity(dto);
//
//        /*
//         * Always use merchant email.
//         */
//        outlet.setOutletEmail(merchant.getMerchantEmail());
//
//        /*
//         * Set location.
//         */
//        outlet.setOutletLocation(location);
//
//        /*
//         * Save outlet.
//         */
//        outlet = outletRepository.save(outlet);
//
//        log.info("[OUTLET_BULK] Outlet saved successfully: outletId={}, outletName={}", outlet.getOutletId(), outlet.getOutletName());
//
//        /*
//         * Save Address.
//         */
//        saveAddress(dto, outlet.getOutletId());
//
//        /*
//         * Save Operating Days.
//         */
//        saveOperatingDays(dto, outlet.getOutletId());
//
//        /*
//         * Save Outlet User.
//         */
//        saveOutletUser(dto.getUsername(), dto.getPassword(), outlet.getOutletId());
//
//        /*
//         * Email #3:
//         * Outlet Registration Successful.
//         *
//         * This executes only after all outlet
//         * creation operations above succeed.
//         */
//        emailService.sendOutletRegistrationEmail(outlet.getOutletEmail(), outlet.getOutletName(), merchant.getMerchantName());
//
//        log.info("[OUTLET_BULK] Outlet registration email triggered: outletId={}, email={}", outlet.getOutletId(), outlet.getOutletEmail());
//
//        return FmOutletMapper.toCreatedDTO(outlet);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FmOutletCreatedDTO createOutletBulkUpload(FmOutletRequestDTO dto) {

        log.info(
                "[OUTLET_BULK] START | outletName={} | merchantName={} | phone={}",
                dto.getOutletName(),
                dto.getMerchantName(),
                dto.getOutletPhone()
        );

        // ============================================================
        // 1. RESOLVE MERCHANT NAME -> MERCHANT ID
        // ============================================================

        if (isBlank(dto.getMerchantName())) {
            throw new IllegalArgumentException(
                    "Merchant Name is required for bulk upload."
            );
        }

        FmMerchant merchant = merchantRepository
                .findMerchantByName(dto.getMerchantName().trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Merchant not found with name: "
                                        + dto.getMerchantName()
                        )
                );

        dto.setMerchantId(merchant.getMerchantId());

        // ============================================================
        // 2. RESOLVE STATE NAME -> STATE ID
        // ============================================================

        Integer stateId = resolveStateId(dto.getStateName());
        dto.setStateId(stateId);

        // ============================================================
        // 3. RESOLVE CITY NAME -> CITY ID
        // ============================================================

        if (isBlank(dto.getCityName())) {
            throw new IllegalArgumentException(
                    "City Name is required for bulk upload."
            );
        }

        FmCity city = cityRepository
                .findByStateId(stateId)
                .stream()
                .filter(c ->
                        c.getCityName() != null
                                && c.getCityName()
                                .trim()
                                .equalsIgnoreCase(
                                        dto.getCityName().trim()
                                )
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "City '" + dto.getCityName()
                                        + "' not found for state '"
                                        + dto.getStateName() + "'."
                        )
                );

        dto.setCityId(city.getCityId());

        // ============================================================
        // 4. RESOLVE AREA NAME -> AREA ID
        // ============================================================

        Integer areaId = resolveAreaId(dto.getAreaName());
        dto.setAreaId(areaId);

        // ============================================================
        // 5. RESOLVE CUISINE NAMES -> CUISINE IDS
        // ============================================================

        resolveCuisineNames(dto);

        // ============================================================
        // 6. VALIDATE REQUEST
        //
        // Validation is done AFTER name -> ID conversion because
        // validateOutletRequest() expects merchantId and cuisineType.
        // ============================================================

        validateOutletRequest(dto);

        // ============================================================
        // 7. CHECK OUTLET PHONE
        // ============================================================

        if (outletRepository.existsByOutletPhone(dto.getOutletPhone())) {
            throw new IllegalArgumentException(
                    "An outlet with phone "
                            + dto.getOutletPhone()
                            + " already exists."
            );
        }

        // ============================================================
        // 8. CHECK DUPLICATE OUTLET
        //
        // Same merchant + same outlet name + same area = duplicate.
        // ============================================================

        if (outletRepository.existsByMerchantAndOutletNameAndArea(
                dto.getMerchantId(),
                dto.getOutletName(),
                dto.getAreaId()
        )) {
            throw new IllegalArgumentException(
                    "Outlet '" + dto.getOutletName()
                            + "' already exists for this merchant "
                            + "in the selected area."
            );
        }

        // ============================================================
        // 9. USERNAME
        //
        // If username is supplied in CSV/Excel, use it.
        // If blank, generate automatically.
        // ============================================================

        String username = dto.getUsername();

        if (isBlank(username)) {
            username = generateUniqueOutletUsername(
                    dto.getOutletName()
            );

            log.info(
                    "[OUTLET_BULK] USERNAME_GENERATED | outletName={} | username={}",
                    dto.getOutletName(),
                    username
            );
        }

        // ============================================================
        // 10. PASSWORD
        //
        // If password is supplied in CSV/Excel, use it.
        // If blank, generate automatically.
        // ============================================================

        String password = dto.getPassword();

        if (isBlank(password)) {
            password = generateOutletPassword();

            log.info(
                    "[OUTLET_BULK] PASSWORD_GENERATED | outletName={}",
                    dto.getOutletName()
            );
        }

        // ============================================================
        // 11. CHECK USERNAME
        // ============================================================

        if (userRepository
                .findByUsernameAndUserType(
                        username,
                        FmAppConstants.TYPE_OUTLET
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Username already exists: " + username
            );
        }

        dto.setUsername(username);
        dto.setPassword(password);

        // ============================================================
        // 12. OUTLET EMAIL
        //
        // Email validation and email sending are NOT required for
        // bulk upload. Do not validate CSV outlet email.
        //
        // Keep merchant email on the outlet only if the database
        // requires outlet_email to be populated. No email is sent.
        // ============================================================

        dto.setOutletEmail(
                merchant.getMerchantEmail()
        );

        // ============================================================
        // 13. BUILD OUTLET LOCATION
        // ============================================================

        Point location = buildPoint(
                dto.getLatitude(),
                dto.getLongitude()
        );

        // ============================================================
        // 14. CREATE OUTLET
        // ============================================================

        FmOutlet outlet = FmOutletMapper.toEntity(dto);

        outlet.setMerchantId(
                merchant.getMerchantId()
        );

        outlet.setOutletEmail(
                merchant.getMerchantEmail()
        );

        outlet.setOutletLocation(location);

        // New bulk outlet remains pending approval.
        outlet.setIsApproved(false);

        outlet = outletRepository.save(outlet);

        log.info(
                "[OUTLET_BULK] OUTLET_CREATED | outletId={} | outletName={}",
                outlet.getOutletId(),
                outlet.getOutletName()
        );

        // ============================================================
        // 15. CREATE OUTLET ADDRESS
        // ============================================================

        saveAddress(
                dto,
                outlet.getOutletId()
        );

        // ============================================================
        // 16. CREATE OUTLET BANK DETAILS
        // ============================================================

        saveOutletBankDetails(
                dto,
                outlet.getOutletId()
        );

        // ============================================================
        // 17. CREATE OUTLET KYC
        // ============================================================

        saveOutletKyc(
                dto,
                outlet.getOutletId()
        );

        // ============================================================
        // 18. CREATE OPERATING DAYS / TIMINGS
        // ============================================================

        saveBulkOperatingDays(
                dto,
                outlet.getOutletId()
        );

        // ============================================================
        // 19. CREATE OUTLET LOGIN USER
        // ============================================================

        saveOutletUser(
                username,
                password,
                outlet.getOutletId()
        );

        // ============================================================
        // 20. CREATE APPROVAL REQUEST
        //
        // Reuse the SAME approval service used by the existing
        // single-outlet flow. Do not change the old createOutlet()
        // function.
        // ============================================================

        approvalRequestService.createApprovalRequest(
                FmAppConstants.TYPE_OUTLET,
                outlet.getOutletId(),
                outlet.getOutletId()
        );

        log.info(
                "[OUTLET_BULK] APPROVAL_REQUEST_CREATED | outletId={} | status=PENDING",
                outlet.getOutletId()
        );

        // ============================================================
        // 21. BULK UPLOAD EMAIL
        // ============================================================
        // Email is intentionally NOT sent during bulk upload.
        // Bulk upload must not fail because of SMTP/AWS SES credentials.

        // ============================================================
        // 22. RETURN RESULT
        // ============================================================

        return FmOutletMapper.toCreatedDTO(outlet);
    }

    // ── Bulk Upload ───────────────────────────────────────────────────────────

 /*@Transactional(rollbackFor =  Exception.class)
    @Override
    public FmBulkOutletResultDTO bulkUpload(List<FmOutletRequestDTO> rows) {
        int total = rows.size(), success = 0;
        List<FmBulkOutletResultDTO.OutletCredential> credentials = new ArrayList<>();
        List<FmBulkOutletResultDTO.OutletError>      errors      = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 3;
            FmOutletRequestDTO dto = rows.get(i);
            try {
                FmOutletCreatedDTO created = createOutlet(dto);
                success++;
                FmBulkOutletResultDTO.OutletCredential cred = new FmBulkOutletResultDTO.OutletCredential();
                cred.setOutletId(created.getOutletId());
                cred.setOutletName(created.getOutletName());
                cred.setOutletLoginId(created.getOutletLoginId());
                cred.setOutletPassword(created.getOutletPassword());
                credentials.add(cred);
            } catch (Exception e) {
                log.warn("[BULK] Row {} failed: {}", rowNum, e.getMessage());
                FmBulkOutletResultDTO.OutletError err = new FmBulkOutletResultDTO.OutletError();
                err.setRowNumber(rowNum);
                err.setOutletName(dto.getOutletName());
                err.setReason(e.getMessage());
                errors.add(err);
                throw new RuntimeException(e.getMessage());
            }
        }

        FmBulkOutletResultDTO result = new FmBulkOutletResultDTO();
        result.setTotalRows(total);
        result.setSuccessCount(success);
        result.setFailureCount(total - success);
        result.setCredentials(credentials);
        result.setErrors(errors);
        return result;
    }*/

    // ============================================================
    // BULK UPLOAD HELPERS
    // These helpers are used only by createOutletBulkUpload().
    // Existing single-outlet functions are not modified.
    // ============================================================

    /**
     * Resolves comma-separated cuisine names from CSV/Excel
     * into cuisine type IDs.
     *
     * Example:
     * Biryani,Chinese,North Indian
     *
     * becomes:
     * [1, 2, 5]
     */
    private void resolveCuisineNames(FmOutletRequestDTO dto) {

        if (isBlank(dto.getCuisineTypeNames())) {
            throw new IllegalArgumentException(
                    "Cuisine Type is required for bulk upload."
            );
        }

        String[] cuisineNames =
                dto.getCuisineTypeNames().split(",");

        List<Integer> cuisineIds =
                new ArrayList<>();

        for (String cuisineName : cuisineNames) {

            String name = cuisineName.trim();

            if (name.isBlank()) {
                continue;
            }

            FmCuisineType cuisineType =
                    cuisineTypeRepository
                            .findByCuisineTypesNameIgnoreCase(name)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Cuisine Type '"
                                                    + name
                                                    + "' not found."
                                    )
                            );

            cuisineIds.add(
                    cuisineType.getCuisineTypesId()
            );
        }

        if (cuisineIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one valid cuisine type is required."
            );
        }

        dto.setCuisineType(
                cuisineIds.toArray(new Integer[0])
        );
    }

    /**
     * Generates a unique outlet username from the outlet name.
     *
     * Example:
     * Mehfil Restaurant
     * -> mehfil_restaurant
     *
     * If already exists:
     * -> mehfil_restaurant_1
     * -> mehfil_restaurant_2
     */
    private String generateUniqueOutletUsername(
            String outletName
    ) {

        String base = outletName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (base.isBlank()) {
            base = "outlet";
        }

        if (base.length() > 35) {
            base = base.substring(0, 35);
        }

        String username = base;
        int counter = 1;

        while (userRepository
                .findByUsernameAndUserType(
                        username,
                        FmAppConstants.TYPE_OUTLET
                )
                .isPresent()) {

            username = base + "_" + counter;
            counter++;
        }

        return username;
    }

    /**
     * Generates a password satisfying the existing outlet
     * password policy:
     *
     * - uppercase
     * - lowercase
     * - number
     * - special character
     */
    private String generateOutletPassword() {

        String upper =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        String lower =
                "abcdefghijklmnopqrstuvwxyz";

        String numbers =
                "0123456789";

        String special =
                "@#$%&!";

        SecureRandom random =
                new SecureRandom();

        StringBuilder password =
                new StringBuilder();

        // Guarantee at least one of each required type.
        password.append(
                upper.charAt(
                        random.nextInt(upper.length())
                )
        );

        password.append(
                lower.charAt(
                        random.nextInt(lower.length())
                )
        );

        password.append(
                numbers.charAt(
                        random.nextInt(numbers.length())
                )
        );

        password.append(
                special.charAt(
                        random.nextInt(special.length())
                )
        );

        String all =
                upper + lower + numbers + special;

        while (password.length() < 12) {

            password.append(
                    all.charAt(
                            random.nextInt(all.length())
                    )
            );
        }

        List<Character> characters =
                new ArrayList<>();

        for (char c : password.toString().toCharArray()) {
            characters.add(c);
        }

        Collections.shuffle(
                characters,
                random
        );

        StringBuilder result =
                new StringBuilder();

        for (Character c : characters) {
            result.append(c);
        }

        return result.toString();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateOutletRequest(FmOutletRequestDTO dto) {
        List<String> errs = new ArrayList<>();

        if (isBlank(dto.getOutletName())) errs.add("Outlet name is required");
        else if (dto.getOutletName().length() > 100) errs.add("Outlet name must not exceed 100 characters");

        if (dto.getMerchantId() == null) errs.add("Merchant ID is required");
        if (dto.getCuisineType() == null || dto.getCuisineType().length == 0) {
            errs.add("Cuisine type is required");
        } else if (Arrays.stream(dto.getCuisineType()).anyMatch(Objects::isNull)) {
            errs.add("Cuisine type contains an invalid value");
        }
        if (isBlank(dto.getOutletPhone())) errs.add("Outlet phone is required");
        else if (!dto.getOutletPhone().trim().matches("^[6-9]\\d{9}$"))
            errs.add("Outlet phone must be a valid 10-digit Indian mobile number, got: '" + dto.getOutletPhone().trim() + "'");

        if (!isBlank(dto.getBuildingNumber()) || !isBlank(dto.getRoad())) {
            if (isBlank(dto.getAreaName())) {
                errs.add("Area name is required when address is provided");
            } else if (dto.getAreaName().trim().length() > 50) {
                errs.add("Area name must not exceed 50 characters");
            }
            if (!isBlank(dto.getBuildingNumber()) && dto.getBuildingNumber().trim().length() > 50)
                errs.add("Building number must not exceed 50 characters");
            if (!isBlank(dto.getRoad()) && dto.getRoad().trim().length() > 100)
                errs.add("Road must not exceed 100 characters");
            if (!isBlank(dto.getLandmark()) && dto.getLandmark().trim().length() > 150)
                errs.add("Landmark must not exceed 150 characters");
        }

        if (!errs.isEmpty()) throw new IllegalArgumentException("Validation failed: " + String.join("; ", errs));
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    //    not used for creating single outlet used in bulk upload only
    private Integer resolveStateId(String stateName) {
        if (isBlank(stateName)) throw new IllegalArgumentException("State name is required");
        return stateRepository.findByStateNameIgnoreCase(stateName.trim()).orElseThrow(() -> new IllegalArgumentException("State '" + stateName.trim() + "' not found in states table.")).getStateId();
    }

    /**
     * Resolves an area name (as supplied in the upload sheet's ZipCode column) to
     * the integer area_id PK stored in jippy_fm.area.
     *
     * <p>Why: the address table stores area_id (FK), not a free-text area name.
     * Upload sheets use a human-readable name; this lookup bridges that gap.</p>
     *
     * @param areaName the area name string from the upload row
     * @return the matching area_id integer
     * @throws IllegalArgumentException if the name is blank or not found in the area table
     */

    //    not used for creating single outlet used in bulk upload only
    private Integer resolveAreaId(String areaName) {
        if (isBlank(areaName)) throw new IllegalArgumentException("Area name is required");
        return areaRepository.findByAreaNameIgnoreCase(areaName.trim()).orElseThrow(() -> new IllegalArgumentException("Area '" + areaName.trim() + "' not found in area table.")).getAreaId();
    }

    private Point buildPoint(String latStr, String lonStr) {
        if (isBlank(latStr) || isBlank(lonStr)) return null;
        try {
            double lat = Double.parseDouble(latStr.trim());
            double lon = Double.parseDouble(lonStr.trim());
            Point p = GEO_FACTORY.createPoint(new Coordinate(lon, lat));
            p.setSRID(4326);
            return p;
        } catch (NumberFormatException e) {
            log.warn("[OUTLET] Could not parse lat/lon: lat='{}', lon='{}' — skipping", latStr, lonStr);
            return null;
        }
    }

    /*
     * Save outlet address.
     *
     * The Create Outlet API sends State ID, City ID and Area ID
     * directly from the UI. Validate that they exist before saving.
     */
    private void saveAddressUsingIds(FmOutletRequestDTO dto, Integer outletId) {

        /* comment during live
         * Validate State.
         */
        stateRepository.findById(dto.getStateId()).orElseThrow(() -> new ResourceNotFoundException("State not found with id : " + dto.getStateId()));

        /* comment during live
         * Validate City.
         */
        cityRepository.findById(dto.getCityId()).orElseThrow(() -> new ResourceNotFoundException("City not found with id : " + dto.getCityId()));

        /* comment during live
         * Validate Area.
         */
        areaRepository.findById(dto.getAreaId()).orElseThrow(() -> new ResourceNotFoundException("Area not found with id : " + dto.getAreaId()));

        /*
         * Convert DTO to Address DTO.
         */
        FmAddressRequestDto addressReqDto = FmOutletMapper.convertToAddressReqDto(dto, outletId, dto.getStateId(), dto.getAreaId());
        /*
         * Convert Address DTO to Entity.
         */
        FmOutletAddress address = FmOutletMapper.toAddressEntity(addressReqDto);

        addressRepository.save(address);

        log.info("Outlet address saved successfully for outletId : {}", outletId);
    }

    private void saveAddress(FmOutletRequestDTO dto, Integer outletId) {
        if (isBlank(dto.getBuildingNumber()) && isBlank(dto.getRoad())) return;
        Integer stateId = resolveStateId(dto.getStateName());
        Integer areaId = resolveAreaId(dto.getAreaName());
        FmAddressRequestDto AddressReqDto = FmOutletMapper.convertToAddressReqDto(dto, outletId, stateId, areaId);
        FmOutletAddress address = FmOutletMapper.toAddressEntity(AddressReqDto);
//        Integer areaId = resolveAreaId(dto.getAreaName());
//        FmOutletAddress address = FmOutletMapper.toAddressEntity(dto, outletId, stateId, areaId);
        addressRepository.save(address);
        log.info("[OUTLET] Address saved for outletId={}", outletId);
    }

    /**
     * Saves operating-day slots for BULK UPLOAD only.
     *
     * The bulk CSV/Excel parser can create multiple FmOutletDayDTO rows
     * for the same day (for example, Monday 09:00-14:00 and Monday
     * 18:00-22:00). Each DTO is intentionally stored as a separate
     * FmOutletDay row.
     *
     * This method is kept separate from saveOperatingDays() so the
     * existing single-outlet create/update flow is not changed.
     */
    private void saveBulkOperatingDays(FmOutletRequestDTO dto, Integer outletId) {

        if (dto.getOperatingDays() == null || dto.getOperatingDays().isEmpty()) {
            log.info(
                    "[OUTLET_BULK] No operating slots provided | outletId={}",
                    outletId
            );
            return;
        }

        int savedSlots = 0;

        for (FmOutletDayDTO d : dto.getOperatingDays()) {

            if (d == null || d.getDayOfWeekId() == null) {
                continue;
            }

            boolean isEvening =
                    d.getSlotType() != null
                            && "evening".equalsIgnoreCase(d.getSlotType());

            LocalTime defaultOpeningTime =
                    isEvening ? LocalTime.of(17, 0) : LocalTime.of(9, 0);

            LocalTime defaultClosingTime =
                    isEvening ? LocalTime.of(22, 0) : LocalTime.of(14, 0);

            FmOutletDay day = new FmOutletDay();

            day.setOutletId(outletId);
            day.setDayOfWeekId(d.getDayOfWeekId());
            day.setIsOpen(
                    d.getIsOpen() != null
                            ? d.getIsOpen()
                            : true
            );

            day.setOpeningTime(
                    parseTime(
                            d.getOpeningTime() == null
                                    ? null
                                    : String.valueOf(d.getOpeningTime()),
                            defaultOpeningTime
                    )
            );

            day.setClosingTime(
                    parseTime(
                            d.getClosingTime() == null
                                    ? null
                                    : String.valueOf(d.getClosingTime()),
                            defaultClosingTime
                    )
            );

            dayRepository.save(day);
            savedSlots++;

            log.info(
                    "[OUTLET_BULK] OPERATING_SLOT_CREATED | outletId={} | dayOfWeekId={} | opening={} | closing={} | slotType={}",
                    outletId,
                    d.getDayOfWeekId(),
                    day.getOpeningTime(),
                    day.getClosingTime(),
                    d.getSlotType()
            );
        }

        log.info(
                "[OUTLET_BULK] Operating slots saved | outletId={} | slots={}",
                outletId,
                savedSlots
        );
    }

    private void saveOperatingDays(FmOutletRequestDTO dto, Integer outletId) {
        if (dto.getOperatingDays() == null || dto.getOperatingDays().isEmpty()) return;
        for (FmOutletDayDTO d : dto.getOperatingDays()) {
            if (d.getDayOfWeekId() == null) continue;

            /*
             * If slotType is not provided,
             * treat it as a normal/full-day slot.
             */
            boolean isEvening = d.getSlotType() != null && "evening".equalsIgnoreCase(d.getSlotType());

//            boolean isEvening = "evening".equalsIgnoreCase(d.getSlotType());
            LocalTime defOpen = isEvening ? LocalTime.of(17, 0) : LocalTime.of(9, 0);
            LocalTime defClose = isEvening ? LocalTime.of(22, 0) : LocalTime.of(14, 0);

            FmOutletDay day = new FmOutletDay();
            day.setOutletId(outletId);
            day.setDayOfWeekId(d.getDayOfWeekId());
            day.setIsOpen(d.getIsOpen() != null ? d.getIsOpen() : true);
            day.setOpeningTime(parseTime(String.valueOf(d.getOpeningTime()), defOpen));
            day.setClosingTime(parseTime(String.valueOf(d.getClosingTime()), defClose));
            dayRepository.save(day);
        }
        log.info("[OUTLET] Operating slots saved for outletId={}", outletId);
    }

    private void saveOutletUser(String username, String password, Integer outletId) {

        Optional<FmUser> existingUser = userRepository.findByUsernameAndUserType(username, FmAppConstants.TYPE_OUTLET);

        if (existingUser.isPresent()) {

            throw new ResourceNotFoundException("Username already exists.");
        }

        String encodedPassword = passwordEncoder.encode(password);

        FmUser user = FmMerchantMapper.toUserEntity(username, encodedPassword, outletId, FmAppConstants.TYPE_OUTLET);

        user = userRepository.save(user);

        log.info("[OUTLET] User created successfully. Username={}", username);

        FmRoles role = roleRepository.findByRoleName(FmAppConstants.ROLE_OUTLET);

        if (role == null) {
            throw new RuntimeException("Role not found");
        }

        List<FmRolePermissions> permissions = rolePermissionsRepository.findByRole(role);

        if (permissions.isEmpty()) {
            throw new RuntimeException("No permissions mapped to role");
        }

        for (FmRolePermissions permission : permissions) {

            FmUserRolePermissions userRole = FmMerchantMapper.toUserRolesEntity(user, permission);

            userRolesRepository.save(userRole);
        }

        log.info("[OUTLET] Role mapping completed for username={}", username);
    }

    private LocalTime parseTime(String s, LocalTime fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            return s.length() == 5 ? LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm")) : LocalTime.parse(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    //    for api to get outlet details by outlet id and user type
//    (merchant or customer) service implementation
    @Override
    public FmOutletDetailsDto getOutletDetails(Integer outletId, String userType, Integer customerId) {

        log.info("Fetching outlet details for outletId={}, userType={}, customerId={}", outletId, userType, customerId);

        boolean outletExists = outletRepository.existsByOutletIdAndIsApprovedTrue(outletId);

        // =========================================================
        // STEP 1: Check outlet existence
        // =========================================================

        if (!outletExists) {

            log.warn("Outlet not found | outletId={}", outletId);

            throw new ResourceNotFoundException("Outlet not found with id: " + outletId);
        }
//---------------------------------------------------------------------------------------------------
        FmOutletDetailsDto outletDtoresponse = new FmOutletDetailsDto();

        // 1.1 Construct userType-specific Redis Key
        String cacheKey = String.format("menu:outlet:%d:type:%s", outletId, userType.toUpperCase());
        try {
            // =========================================================================
            // STEP 1: CHECK REDIS FIRST (Fast Path)
            // =========================================================================
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null) {
                log.info("Cache HIT for outlet menu key: {}", cacheKey);
                return objectMapper.readValue(cachedJson, FmOutletDetailsDto.class);
            }

            // =========================================================================
            // STEP 2: FETCH BASE DATA BASED ON USER TYPE
            // =========================================================================

            LocalDateTime earliestEndTime = null;

            if (FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(userType)) {

                // ---------------------------------------------------------------------
                // MERCHANT FLOW
                // Fetch complete outlet menu using the merchant-specific query.
                //
                // Merchant query also fetches:
                // outlets.cuisine_type
                //        ↓
                // cuisine_types.cuisine_types_id
                // cuisine_types.cuisine_types_name
                // ---------------------------------------------------------------------

                log.info("Fetching merchant outlet menu | outletId={}", outletId);

                List<FmMerchantOutletMenuProjection> merchantRows = outletRepository.getMerchantOutletMenu(outletId);

                // Validate merchant outlet menu data
                if (merchantRows == null || merchantRows.isEmpty()) {

                    log.warn("No merchant menu available | outletId={}", outletId);

                    throw new ResourceNotFoundException("No menu available for outlet: " + outletId);
                }

                log.info("Merchant outlet menu fetched | outletId={} | rows={}", outletId, merchantRows.size());

                // Map merchant projection → Outlet Details DTO
                outletDtoresponse = FmOutletMapper.mapMerchantToOutletDto(merchantRows);

            } else {

                // ---------------------------------------------------------------------
                // CUSTOMER FLOW
                // Use the existing outlet menu query.
                //
                // Customer response contains the existing product/menu details
                // including customer-specific online pricing.
                // ---------------------------------------------------------------------

                log.info("Fetching customer outlet menu | outletId={}", outletId);

                List<FmOutletMenuProjection> rows = outletRepository.getCustomerOutletMenu(outletId);

                // Validate customer outlet menu data
                if (rows == null || rows.isEmpty()) {

                    log.warn("No customer menu available for outlet ID: " + outletId + ".\n" + "Required menu data is missing.\n" + "Data must exist in the following DB tables:\n" + "1. jippy_fm.outlets\n" + "2. jippy_fm.outlet_days (Outlet Timings)\n" + "3. jippy_fm.outlet_categories\n" + "4. jippy_fm.categories\n" + "5. jippy_fm.products\n" + "6. jippy_fm.product_available_timings(product timings)");


                    throw new ResourceNotFoundException("No customer menu available for outlet ID: " + outletId + ".   \n" + "  Required menu data is missing.  \n" + "  Data must exist in the following DB tables:  \n" + "  1. jippy_fm.outlets  \n" + "  2. jippy_fm.outlet_days (Outlet Timings)  \n" + "  3. jippy_fm.outlet_categories  \n" + "  4. jippy_fm.categories  \n" + "  5. jippy_fm.products\n" + "  6. jippy_fm.product_available_timings(product timings)");
                }

                log.info("Customer outlet menu fetched | outletId={} | rows={}", outletId, rows.size());

                // Map existing customer projection → Outlet Details DTO
                outletDtoresponse = FmOutletMapper.mapCustomerToOutletDto(rows, userType);
            }

//           ================================================================================

            log.info("============ OutletDtoResponse ================" + outletDtoresponse);

            if (FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType)) {

                // =========================================================================
                // STEP 3: Fetch active discounts
                // =========================================================================

                List<Integer> outletIds = new ArrayList<>();
                outletIds.add(outletId);
                //Get active promotions
                List<FmActivePromotionDiscountsProjection> activePromotionDiscountsProjections = promotionPlanRepository.getActivePromtionDiscounts(LocalDateTime.now(), outletIds);

                log.info("Active Promotion Discounts {}", activePromotionDiscountsProjections);

                //Get active coupons and price drops
                ResponseEntity<List<FmActiveDiscountsResponseDto>> rawActiveDiscountsResponse = divisionFeignClient.getActiveDiscounts();

                List<FmActiveDiscountsResponseDto> rawActiveDiscounts = rawActiveDiscountsResponse.getBody();

                // EARLY EXIT GUARD: If both sources are completely empty, skip ALL processing
                boolean hasPromos = activePromotionDiscountsProjections != null && !activePromotionDiscountsProjections.isEmpty();
                boolean hasDivisionDiscounts = rawActiveDiscounts != null && !rawActiveDiscounts.isEmpty();

                if (!hasPromos && !hasDivisionDiscounts) {
                    log.info("Zero active promotions or coupons found. Proceeding with standard menu pricing.");

                    // Cache the raw menu in Redis with standard 10-minute TTL so subsequent calls hit Redis Step 1
                    saveToRedis(cacheKey, outletDtoresponse, 30, TimeUnit.MINUTES);

                    return outletDtoresponse;
                }

                // -------------------------------------------------------------------------
                //  Filter Division Discounts by FM Meal Type Slot Timings
                // -------------------------------------------------------------------------
                LocalTime currentTime = LocalTime.now();

                List<FmActiveDiscountsResponseDto> activeDiscountsResponseDtoList = Optional.ofNullable(rawActiveDiscounts).orElse(Collections.emptyList()).stream().filter(discount -> isDiscountActiveInCurrentSlot(discount, currentTime)).collect(Collectors.toList());

                log.info("Division discounts valid for current meal slot ({}): {}", currentTime, activeDiscountsResponseDtoList);

                // Index Merchant Promotion Projections by outletId (Priority 2)
                Map<Integer, List<FmActivePromotionDiscountsProjection>> merchantPromotionsMap = Optional.ofNullable(activePromotionDiscountsProjections).orElse(Collections.emptyList()).stream().filter(p -> p.getOutletId() != null).collect(Collectors.groupingBy(FmActivePromotionDiscountsProjection::getOutletId));

                // =========================================================================
                // STEP 4: APPLY PRIORITY LOGIC & MAP TO RESPONSE
                // Priority: Merchant Promotion (1) > Division Coupon/Price Drop (2) > None (3)
                // =========================================================================

                if (merchantPromotionsMap.containsKey(outletId)) {

                    log.info("Given Outlet :{} has merchant promotions", outletId);
                    // PRIORITY 1: Merchant Promotion
                    List<FmActivePromotionDiscountsProjection> promos = merchantPromotionsMap.get(outletId);
                    applyMerchantPromotions(outletDtoresponse, promos);

                    // Capture end time for TTL
                    earliestEndTime = promos.stream().map(FmActivePromotionDiscountsProjection::getEndDateTime).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);

                } else {

                    log.info("Given Outlet :{} has coupons/price drops ", outletId);

                    // Index Division Active Discounts by outletId (Priority 1)
                    // (Grouping by outletId in case an outlet has multiple discounts, or toMap if 1 discount per outlet)
                    Map<Integer, List<FmActiveDiscountsResponseDto>> divisionDiscountsMap = Optional.ofNullable(activeDiscountsResponseDtoList).orElse(Collections.emptyList()).stream().filter(d -> d.getOutletId() != null).collect(Collectors.groupingBy(FmActiveDiscountsResponseDto::getOutletId));

                    // PRIORITY 2: Division Coupon / Price Drop
                    List<FmActiveDiscountsResponseDto> feignDiscounts = divisionDiscountsMap.get(outletId);

                    // SAFE GUARD: Check if feignDiscounts is actually present for this outlet
                    if (feignDiscounts != null && !feignDiscounts.isEmpty()) {
                        applyDivisionDiscounts(outletDtoresponse, feignDiscounts);

                        // Calculate the earliest slot end time among active discounts
                        earliestEndTime = feignDiscounts.stream().map(discount -> getActiveMealSlotEndTime(discount, LocalDateTime.now())).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
                    } else {
                        log.info("No division discounts match outletId={}", outletId);
                    }
                }

            }

            // =========================================================================
            // STEP 5: PUSH DATA TO REDIS (Lazy Write)
            // =========================================================================

            LocalDateTime now = LocalDateTime.now();

            if (FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType) && earliestEndTime != null && earliestEndTime.isAfter(now)) {
                // TTL matches either current meal slot end time OR merchant plan end date
                long ttlSeconds = Duration.between(now, earliestEndTime).getSeconds();

                // Safety guard: ensure TTL is at least 30 seconds
                ttlSeconds = Math.max(30, ttlSeconds);

                saveToRedis(cacheKey, outletDtoresponse, ttlSeconds, TimeUnit.SECONDS);

                //redisTemplate.opsForValue().set(cacheKey, jsonPayload, ttlSeconds, TimeUnit.SECONDS);
                // log.info("Cached CUSTOMER view [{}] in Redis with offer/slot TTL: {}s", cacheKey, ttlSeconds);
            } else {
                // Merchant view or No-Offer Customer view: Standard 10 min TTL
                saveToRedis(cacheKey, outletDtoresponse, 30, TimeUnit.MINUTES);
                //  redisTemplate.opsForValue().set(cacheKey, jsonPayload, 5, TimeUnit.MINUTES);
                //  log.info("Cached [{}] in Redis with standard TTL: 300s", cacheKey);
            }


            log.debug("Added For UI - Outlet availability : {}", outletDtoresponse.getIsAvailable());
//      -----------------------------------------------------------------------------------
            /*
             * Default value.
             * If customerId is not passed,
             * favourite should be false.
             */
            outletDtoresponse.setIsFavourite(false);

//        Check favourite only for CUSTOMER.
            if (FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType) && customerId != null) {

                log.info("Checking favourite status for customerId={} and outletId={}", customerId, outletId);

//      ----------------------------------------------------------------------------
                Optional<FmFavoriteOutlet> favourite = favoriteOutletRepository.findByCustomerIdAndFavoriteIdAndFavouriteType(customerId, outletId, FmAppConstants.TYPE_OUTLET);

                //  favourite.isPresent() --> returns true if record exists in the favourite table
                outletDtoresponse.setIsFavourite(favourite.isPresent());

                log.info("Favourite status: {}", favourite.isPresent());

                /*
                 * Populate product favourite status for logged-in customer.
                 */
                log.info("Checking is_product_favourite status");

                setProductFavouriteStatus(outletDtoresponse, customerId);

            }
//  ---------------------------------------------------------------------------

            log.info("Successfully fetched outlet details for outletId={}", outletId);

            return outletDtoresponse;
        }
//        catch (Exception e) {
//            log.error("Corrupted cache payload for key: {}, fetching from DB", cacheKey, e);
//        }
//
//        return outletDtoresponse;
//
//    }
        catch (ResourceNotFoundException e) {

            // Business exception → do not swallow it.
            // Let it reach the global exception handler.
            throw e;

        } catch (Exception e) {

            // Unexpected exception → log it.
            log.error("Unexpected error while fetching outlet details | cacheKey={}", cacheKey, e);
        }

        return outletDtoresponse;
    }
//    ============================================================================================

    private void saveToRedis(String cacheKey, FmOutletDetailsDto outletDtoresponse, long time, TimeUnit timeUnit) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(outletDtoresponse);
            LocalDateTime now = LocalDateTime.now();

            redisTemplate.opsForValue().set(cacheKey, jsonPayload, time, timeUnit);
            log.info("Cached [{}] in Redis with standard TTL:{} {} ", cacheKey, time, timeUnit);
        } catch (Exception e) {
            log.error("An unexpected error occurs in redis push {} ", e.getMessage());
        }


    }

    private boolean isDiscountActiveInCurrentSlot(FmActiveDiscountsResponseDto discount, LocalTime currentTime) {

        // 1. Get list of slot IDs from the comma-separated string ("2,3" -> [2, 3])
        List<Integer> slotIds = discount.getMealTypeSlotIds();

        // If no slots are specified, the offer applies all day
        if (slotIds == null || slotIds.isEmpty()) {
            return true;
        }

        // 2. Fetch all matching slot definitions in a single database query
        List<MealTypeTiming> slots = mealTypeTimingRepository.findAllById(slotIds);

        // If no matching slot records found in DB, default to active (or return false based on your business rule)
        if (slots.isEmpty()) {
            return true;
        }

        // 3. Check if current time falls into ANY of the assigned slots (e.g. Morning OR Evening)
        for (MealTypeTiming slot : slots) {
            LocalTime startTime = slot.getFromTime(); // e.g., 05:00:00
            LocalTime endTime = slot.getToTime();     // e.g., 11:00:00

            if (startTime == null || endTime == null) {
                continue;
            }

            boolean isActiveInThisSlot;

            // Handling overnight slots (e.g., Midnight Dinner 23:00 to 02:00)
            if (endTime.isBefore(startTime)) {
                isActiveInThisSlot = !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
            } else {
                // Standard daytime slot (e.g., Breakfast 05:00 to 11:00)
                isActiveInThisSlot = !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
            }

            // If current time hits ANY valid slot, return true immediately!
            if (isActiveInThisSlot) {
                return true;
            }
        }

        // Current time matches none of the configured slots
        return false;
    }

    private void applyDivisionDiscounts(FmOutletDetailsDto outletDtoresponse, List<FmActiveDiscountsResponseDto> feignDiscounts) {

        if (outletDtoresponse == null || feignDiscounts == null || feignDiscounts.isEmpty()) {
            return;
        }

        // 1. Separate Product-Specific Discounts (Price Drops) and Outlet-Level Discounts (Coupons)
        Map<Integer, FmActiveDiscountsResponseDto> productDiscountsMap = feignDiscounts.stream().filter(d -> d.getProductId() != null).collect(Collectors.toMap(FmActiveDiscountsResponseDto::getProductId, d -> d, (existing, replacement) -> existing));

        // Find outlet-wide coupon (where productId is null)
        Optional<FmActiveDiscountsResponseDto> outletDiscountsDto = feignDiscounts.stream().filter(d -> d.getProductId() == null && ("COUPON".equalsIgnoreCase(d.getSourceType()) || "PRICE_DROP".equalsIgnoreCase(d.getSourceType()))).findFirst();

        // 2. Attach Outlet-Level Offer if present
        if (outletDiscountsDto.isPresent()) {
            FmActiveDiscountsResponseDto outletDiscounts = outletDiscountsDto.get();

            log.info("Outlet level discounts are there  ");

            // Map to activeOffer DTO on root outlet object
            // FmActiveDiscountsResponseDto activeOffer = new FmActiveDiscountsResponseDto();

            FmActiveDiscountsDto activeOffer = FmOutletMapper.mapToActiveDiscounts(outletDiscounts);

            outletDtoresponse.setActiveDiscounts(activeOffer);
        }

        // 3. Apply Product-Level discounts
        if (outletDtoresponse.getCategories() != null && !productDiscountsMap.isEmpty()) {
            for (FmCategoryDto category : outletDtoresponse.getCategories()) {
                if (category.getProducts() == null) continue;

                for (FmProductDto product : category.getProducts()) {

                    log.info("Product level discounts are there ");

                    Integer productId = product.getProductId();

                    if (productDiscountsMap.containsKey(productId)) {
                        FmActiveDiscountsResponseDto productDiscounts = productDiscountsMap.get(productId);

                        FmActiveDiscountsDto activeOffer = FmOutletMapper.mapToActiveDiscounts(productDiscounts);
                        product.setActiveDiscountsDto(activeOffer);

                    }
                }
            }
        }
    }

    private void applyMerchantPromotions(FmOutletDetailsDto outletDtoresponse, List<FmActivePromotionDiscountsProjection> promos) {
        if (outletDtoresponse == null || outletDtoresponse.getCategories() == null || promos == null) {
            return;
        }

        // Index promotions by productId for fast O(1) lookup
        Map<Integer, FmActivePromotionDiscountsProjection> promoByProductMap = promos.stream().filter(p -> p.getProductId() != null).collect(Collectors.toMap(FmActivePromotionDiscountsProjection::getProductId, p -> p, (existing, replacement) -> existing // Keep first in case of duplicates
        ));

        // Iterate through categories and products to apply discounts
        for (FmCategoryDto category : outletDtoresponse.getCategories()) {
            if (category.getProducts() == null) continue;

            for (FmProductDto product : category.getProducts()) {
                Integer productId = product.getProductId();

                if (promoByProductMap.containsKey(productId)) {
                    FmActivePromotionDiscountsProjection promo = promoByProductMap.get(productId);

                    FmActiveDiscountsDto activeDiscountsDto = new FmActiveDiscountsDto();
                    // Set discounted details on product DTO
                    activeDiscountsDto.setPlanType(promo.getPlanType());
                    activeDiscountsDto.setDiscountAmount(promo.getOfferAmount());
                    activeDiscountsDto.setMinOrderValue(promo.getMinimumOrderValue());
                    activeDiscountsDto.setOfferName(promo.getOfferName());
                    activeDiscountsDto.setPriceType(promo.getOfferType());
                    //activeDiscountsDto.setPromotionScheduleId(promo.getPromotion);

                    product.setActiveDiscountsDto(activeDiscountsDto);
                }
            }
        }


    }

    private LocalDateTime getActiveMealSlotEndTime(FmActiveDiscountsResponseDto discount, LocalDateTime now) {
        List<Integer> slotIds = discount.getMealTypeSlotIds();

        // If no slot is attached, fall back to the campaign's endDateTime
        if (slotIds == null || slotIds.isEmpty()) {
            return discount.getEndDateTime();
        }

        List<MealTypeTiming> slots = mealTypeTimingRepository.findAllById(slotIds);
        LocalTime currentTime = now.toLocalTime();

        for (MealTypeTiming slot : slots) {
            LocalTime startTime = slot.getFromTime();
            LocalTime endTime = slot.getToTime();

            if (startTime == null || endTime == null) continue;

            boolean isActive = (endTime.isBefore(startTime)) ? (!currentTime.isBefore(startTime) || !currentTime.isAfter(endTime)) : (!currentTime.isBefore(startTime) && !currentTime.isAfter(endTime));

            if (isActive) {
                // Determine if the active slot ends today or tomorrow morning
                if (endTime.isBefore(startTime) && currentTime.isAfter(startTime)) {
                    // Overnight slot (e.g., 23:00 to 02:00) ending tomorrow
                    return now.plusDays(1).with(endTime);
                } else {
                    // Daytime slot ending today
                    return now.with(endTime);
                }
            }
        }

        // Fallback to plan endDateTime if no slot match is found
        return discount.getEndDateTime();
    }

    /**
     * HELPER_METHOD 1
     * Populate favourite status for every product
     * available in the outlet.
     */
    private void setProductFavouriteStatus(FmOutletDetailsDto outlet, Integer customerId) {

        for (FmCategoryDto category : outlet.getCategories()) {

            for (FmProductDto product : category.getProducts()) {

                log.info("Checking Product Id : {}", product.getProductId());

                Optional<FmFavoriteOutlet> favourite = favoriteOutletRepository.findByCustomerIdAndFavoriteIdAndFavouriteType(customerId, product.getProductId(), FmAppConstants.TYPE_PRODUCT);

                log.info("Repository Result : {}", favourite.isPresent());

                product.setIsProductFavourite(favourite.isPresent());

                log.info("After Setting : {}", product.getIsProductFavourite());
            }
        }
    }

    //    for api to get all outlets by merchant id service implementation
    @Override
    public List<FmOutletByMerchantDto> getOutletsByFmMerchantId(Integer merchantId) {

        log.info("Fetching outlets for merchantId={}", merchantId);

        List<FmOutletByMerchantProjection> rows = outletRepository.getOutletsByMerchantId(merchantId);

        if (rows.isEmpty()) {

            log.warn("No outlets found for merchantId={}", merchantId);

            throw new ResourceNotFoundException("No outlets found for merchantId: " + merchantId);
        }
        log.info("Merchant approved: {}", rows.get(0).getMerchantApproved());


        if (!Boolean.TRUE.equals(rows.get(0).getMerchantApproved())) {

            log.warn("Merchant is not approved. merchantId={}", merchantId);

            throw new BadRequestException("Merchant is not approved. Please complete the approval process.");
        }

        log.info("Successfully fetched {} outlets for merchantId={}", rows.size(), merchantId);

        return FmOutletMapper.mapToOutletByMerchantDto(rows);
    }

    //    for update outlet details by outlet id service implementation
    @Transactional
    @Override
    public FmOutletDetailsDto updateOutletDetails(Integer outletId, FmOutletDetailsDto dto, String userType) {

        // Log start of update operation
        log.info("SERVICE: Updating outlet details for outletId={}, userType={}", outletId, userType);

        // Fetch the outlet from database using outletId
        // If not found, throw exception
        FmOutlet outlet = outletRepository.findById(outletId).orElseThrow(() -> new ResourceNotFoundException("Outlet not found"));

        log.info("SERVICE: Outlet found for outletId={}", outletId);

        // Update outlet timings if they are provided in request
        if (dto.getOutletTimings() != null) {

            // Fetch all existing outlet timing records for this outlet
            List<FmOutletDay> outletDaysList = dayRepository.findByOutletId(outletId);

            // Loop through each timing received in request
            for (FmOutletTimingDto timingDto : dto.getOutletTimings()) {

                // Convert day string (e.g., Monday) into integer id (e.g., 1)
                Integer dayId = getDayId(timingDto.getDay());

                // Loop through existing outlet timing records from database
                for (FmOutletDay outletDay : outletDaysList) {

                    // Match request day with database day
                    if (outletDay.getDayOfWeekId().equals(dayId)) {

                        // Update isOpen only if value is provided
                        if (timingDto.getIsOpen() != null) outletDay.setIsOpen(timingDto.getIsOpen());

                        // Update opening time only if value is provided
                        if (timingDto.getOpeningTime() != null) outletDay.setOpeningTime(timingDto.getOpeningTime());

                        // Update closing time only if value is provided
                        if (timingDto.getClosingTime() != null) outletDay.setClosingTime(timingDto.getClosingTime());
                    }
                }
            }

            // Save all updated outlet timing records
            dayRepository.saveAll(outletDaysList);

            log.debug("SERVICE: Outlet timings updated for outletId={}", outletId);
        }

        // Update categories and products if they are provided in request
        if (dto.getCategories() != null) {

            // Loop through each category from request
            for (FmCategoryDto categoryDto : dto.getCategories()) {

                log.debug("SERVICE: Processing categoryId={}", categoryDto.getCategoryId());

                // Fetch category from database using categoryId
                FmCategory category = categoryRepository.findById(categoryDto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found"));

                // Update category name only if provided
                if (categoryDto.getCategoryName() != null) category.setCategoryName(categoryDto.getCategoryName());

                // Process products inside this category
                if (categoryDto.getProducts() != null) {

                    // Loop through each product from request
                    for (FmProductDto productDto : categoryDto.getProducts()) {

                        log.debug("SERVICE: Updating productId={}", productDto.getProductId());

                        // Fetch product from database using productId
                        FmProduct product = productRepository.findById(productDto.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                        // Update product name if provided
                        if (productDto.getProductName() != null) product.setProductName(productDto.getProductName());

                        // Update description if provided
                        if (productDto.getDescription() != null) product.setDescription(productDto.getDescription());

                        // Update veg flag if provided
                        if (productDto.getIsVeg() != null) product.setIsVeg(productDto.getIsVeg());

                        // Update product variant flag if provided
                        if (productDto.getHasProductVariants() != null)
                            product.setHasProductVariants(productDto.getHasProductVariants());

                        // -------- UPDATE PRODUCT VARIANTS --------
                        if (productDto.getVariants() != null) {

                            for (FmProductVariantDTO vDto : productDto.getVariants()) {

                                // Fetch existing variant from DB
                                FmProductVariant variant = productVariantRepository.findById(vDto.getVariantId()).orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

                                // Update variant name if provided
                                if (vDto.getVariantName() != null) variant.setVariantName(vDto.getVariantName());

                                // Update variant price if provided
                                if (vDto.getOnlinePrice() != null) variant.setMerchantPrice(vDto.getOnlinePrice());
                            }
                        }
                        // Update product price if provided
                        if (productDto.getOnlinePrice() != null) product.setMerchantPrice(productDto.getOnlinePrice());

                        // Update product timings if provided
                        if (productDto.getProductTimings() != null) {

                            // Fetch existing product timing records
                            List<FmProductAvailableTiming> timings = productAvailableTimingRepository.findByProductId(productDto.getProductId());

                            // Loop through each timing from request
                            for (FmProductTimingDto timingDto : productDto.getProductTimings()) {

                                // Convert day string into integer id
                                Integer dayId = getDayId(timingDto.getDay());

                                // Loop through existing product timings
                                for (FmProductAvailableTiming timing : timings) {

                                    // Match request day with database day
                                    if (timing.getDayOfWeekId().equals(dayId)) {

                                        // Update start time if provided
                                        if (timingDto.getStartTime() != null)
                                            timing.setStartTime(timingDto.getStartTime());

                                        // Update end time if provided
                                        if (timingDto.getEndTime() != null) timing.setEndTime(timingDto.getEndTime());
                                    }
                                }
                            }

                            // Save updated product timings
                            productAvailableTimingRepository.saveAll(timings);

                            log.debug("SERVICE: Product timings updated for productId={}", productDto.getProductId());
                        }
                    }
                }
            }
        }

        // Save outlet entity
        // This ensures all changes are persisted and audit fields (if any) are updated
        outletRepository.save(outlet);

        log.info("SERVICE: Successfully updated outlet details for outletId={}", outletId);

        // Returning DTO after update
        // Instead of returning request data, we call GET method
        // This ensures response includes latest DB data along with variants

        // Return latest outlet details after update.
        // customerId is not applicable for update API, so pass null.
        return getOutletDetails(outletId, userType, null);
    }

    //    method to convert day name into integer id used in database
    private Integer getDayId(String day) {

        // Convert string to lowercase and map to corresponding integer value
        switch (day.toLowerCase()) {
            case "monday":
                return 1;
            case "tuesday":
                return 2;
            case "wednesday":
                return 3;
            case "thursday":
                return 4;
            case "friday":
                return 5;
            case "saturday":
                return 6;
            case "sunday":
                return 7;

            // Throw exception if invalid day is passed
            default:
                throw new ResourceNotFoundException("Invalid day: " + day);
        }
    }

    //   for feign client to save address details of driver service implementation
    @Override
    @Transactional
    public FmAddressRequestDto saveAddressDetails(
            FmAddressRequestDto fmAddressRequestDto) {

        Optional<FmOutletAddress> existingAddress =
                addressRepository.findByJippyAddressIdAndAddressType(
                        fmAddressRequestDto.getJippyAddressId(),
                        fmAddressRequestDto.getAddressType());

        FmOutletAddress address;

        if (existingAddress.isPresent()) {

            // Existing address → UPDATE
            address = existingAddress.get();

            address.setBuildingNumber(
                    fmAddressRequestDto.getBuildingNumber());

            address.setRoad(
                    fmAddressRequestDto.getRoad());

            address.setLandmark(
                    fmAddressRequestDto.getLandmark());

            address.setCityId(
                    fmAddressRequestDto.getCityId());

            address.setStateId(
                    fmAddressRequestDto.getStateId());

            address.setAreaId(
                    fmAddressRequestDto.getAreaId());

            // Update location if latitude and longitude are provided
            if (fmAddressRequestDto.getLatitude() != null
                    && fmAddressRequestDto.getLongitude() != null) {

                GeometryFactory geometryFactory =
                        new GeometryFactory();

                Point point = geometryFactory.createPoint(
                        new Coordinate(
                                fmAddressRequestDto.getLongitude(),
                                fmAddressRequestDto.getLatitude()
                        )
                );

                point.setSRID(4326);

                address.setLocation(point);
            }

            log.info(
                    "Updating existing {} address for Jippy Address Id {}, addressId={}",
                    fmAddressRequestDto.getAddressType(),
                    fmAddressRequestDto.getJippyAddressId(),
                    address.getAddressId());

        } else {

            // Address does not exist → CREATE
            address = FmOutletMapper.toAddressEntity(
                    fmAddressRequestDto);

            log.info(
                    "Creating new {} address for Jippy Address Id {}",
                    fmAddressRequestDto.getAddressType(),
                    fmAddressRequestDto.getJippyAddressId());
        }

        FmOutletAddress savedAddress =
                addressRepository.save(address);

        log.info(
                "Address saved successfully with addressId={}",
                savedAddress.getAddressId());

        return FmOutletMapper.toAddressRequestDto(savedAddress);
    }

    //    for feign client to get address details of driver service implementation
    @Override
    @Transactional
    public FmAddressRequestDto getAddressDetails(Integer addressId) {
        FmOutletAddress address = addressRepository.findByJippyAddressId(addressId).orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        FmAddressRequestDto addressResponseDto = FmOutletMapper.toAddressRequestDto(address);
        log.info("address details fetched for address ID  ={}", addressId);
        return addressResponseDto;
    }

    @Override
    public OutletLocationResponseDto getOutletLocation(Integer outletId) {
        // Use parameterized logging to avoid unnecessary String concatenation
        log.info("Fetching location details for outletId: {}", outletId);

        OutletLocationProjection projection = outletRepository.getOutletLocation(outletId);

        if (projection == null) {
            // Log the error locally before throwing to capture the failure context in the server logs
            log.error("Location retrieval failed: Outlet with ID {} does not exist or has no coordinates", outletId);

            throw new ResourceNotFoundException("Outlet not found with ID: " + outletId);
        }

        OutletLocationResponseDto response = new OutletLocationResponseDto();
        response.setOutletId(projection.getOutletId());
        response.setLatitude(projection.getLatitude());
        response.setLongitude(projection.getLongitude());

        log.info("Successfully mapped location data for outletId: {}", outletId);
        return response;
    }

    //    to fetch outlet name by outlet id for order details in customer and order microservices
    public String fetchOutletName(Integer outletId) {

        return outletRepository.fetchOutletName(outletId);
    }

    @Override
    public FmCustomerNearbyResponseDto fetchCustomerNearbyOutlets(double customerLat, double customerLng, Integer categoryId) {

        double radiusKm = FmAppConstants.DEFAULT_RADIUS_KM;

        log.info("[OutletService] fetchCustomerNearbyOutlets lat={} lng={} radius={} km", customerLat, customerLng, radiusKm);

        List<Object[]> rows = outletRepository.findCustomerNearbyOutlets(customerLat, customerLng, categoryId);

        /*
         * NO OUTLETS FOUND
         */

        if (rows.isEmpty()) {

            FmCustomerNearbyResponseDto response = new FmCustomerNearbyResponseDto();

            response.setCustomerLat(customerLat);

            response.setCustomerLng(customerLng);

            response.setRadiusKm(radiusKm);

            response.setTotalOutlets(0);

            response.setMessage("Service is not available in this area");

            response.setOutlets(List.of());

            return response;
        }

        // =========================================================================
        // STEP 1: COLLECT ALL OUTLET IDs FROM ROWS
        // =========================================================================
        List<Integer> outletIds = rows.stream().map(row -> row[0] != null ? ((Number) row[0]).intValue() : null).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        LocalTime curTime = now.toLocalTime();

        List<FmNearbyOutletDto> outlets = new ArrayList<>();

        // =========================================================================
        // STEP 2: BULK FETCH MERCHANT PROMOTIONS (PRIORITY 1)
        // =========================================================================
        // Querying active promotions for all fetched nearby outlets in a single DB call
        List<FmActivePromotionDiscountsProjection> activePromos = promotionPlanRepository.getActivePromtionDiscounts(now, outletIds);

        Map<Integer, List<FmActivePromotionDiscountsProjection>> merchantPromotionsMap = Optional.ofNullable(activePromos).orElse(Collections.emptyList()).stream().filter(p -> p.getOutletId() != null).collect(Collectors.groupingBy(FmActivePromotionDiscountsProjection::getOutletId));

        // =========================================================================
        // STEP 3: BULK FETCH DIVISION DISCOUNTS VIA FEIGN (PRIORITY 2 FALLBACK)
        // =========================================================================
        // Fetch active division coupons/price drops only for outlets that DON'T have merchant promotions
        List<Integer> outletsNeedingDivisionDiscounts = outletIds.stream().filter(id -> !merchantPromotionsMap.containsKey(id)).collect(Collectors.toList());

        Map<Integer, List<FmActiveDiscountsResponseDto>> divisionDiscountsMap = Collections.emptyMap();

        if (!outletsNeedingDivisionDiscounts.isEmpty()) {
            try {
                ResponseEntity<List<FmActiveDiscountsResponseDto>> feignResponse = divisionFeignClient.getActiveDiscounts();
                List<FmActiveDiscountsResponseDto> rawDivisionDiscounts = feignResponse.getBody();

                if (rawDivisionDiscounts != null && !rawDivisionDiscounts.isEmpty()) {
                    divisionDiscountsMap = rawDivisionDiscounts.stream().filter(d -> d.getOutletId() != null && outletsNeedingDivisionDiscounts.contains(d.getOutletId())).filter(discount -> isDiscountActiveInCurrentSlot(discount, curTime)).collect(Collectors.groupingBy(FmActiveDiscountsResponseDto::getOutletId));
                }
            } catch (Exception e) {
                log.error("Failed to fetch division discounts via Feign client in nearby outlets API", e);
            }
        }

        for (Object[] row : rows) {

            FmNearbyOutletDto dto = new FmNearbyOutletDto();

            Integer outletId = row[0] != null ? ((Number) row[0]).intValue() : null;

            dto.setOutletId(outletId);

            dto.setOutletName((String) row[1]);

            dto.setCuisineType((String) row[3]);

            dto.setOutletPhone((String) row[4]);

            dto.setRadius(row[5] != null ? ((Number) row[5]).doubleValue() : null);

            dto.setReview(row[6] != null ? ((Number) row[6]).doubleValue() : null);

            dto.setSubscriptionStatus((String) row[7]);

            dto.setPromotionStatus((String) row[8]);

            dto.setOpeningTime(row[12] != null ? row[12].toString() : null);

            dto.setClosingTime(row[13] != null ? row[13].toString() : null);

            dto.setDistanceKm(row[16] != null ? ((Number) row[16]).doubleValue() : null);

            /*
             * OPEN NOW LOGIC
             */

            Boolean openNow = false;

            if (row[12] != null && row[13] != null) {

                LocalTime openingTime = ((java.sql.Time) row[12]).toLocalTime();

                LocalTime closingTime = ((java.sql.Time) row[13]).toLocalTime();

                LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));

                if (closingTime.isAfter(openingTime)) {

                    openNow = !currentTime.isBefore(openingTime) && currentTime.isBefore(closingTime);

                } else {

                    /*
                     * OVERNIGHT CASE
                     */

                    openNow = !currentTime.isBefore(openingTime) || currentTime.isBefore(closingTime);
                }
            }

            dto.setOpenNow(openNow);

            if (row[19] != null) {
                if (row[19] instanceof Boolean) {
                    dto.setIsVegOutlet((Boolean) row[19]);
                }
            }

            if (row[20] != null) {
                dto.setOutletPicUrl(row[20].toString());
            }
            if (row[21] != null) {
                dto.setIsBestRestaurant((Boolean) row[21]);
            }

            /*
             * GOOGLE MAPS DISTANCE + DELIVERY TIME
             */

            Double outletLat = null;

            Double outletLng = null;

            if (row[17] != null) {

                outletLat = ((Number) row[17]).doubleValue();
            }

            if (row[18] != null) {

                outletLng = ((Number) row[18]).doubleValue();
            }

            if (outletLat != null && outletLng != null) {

                FmGoogleMapsService.DistanceResult maps = googleMapsService.getDistanceAndDuration(customerLat, customerLng, outletLat, outletLng);

                dto.setRoadDistance(maps.roadDistance());

                if (maps.deliveryTime() != null) {

                    dto.setDeliveryTime(maps.deliveryTime());

                } else {

                    /*
                     * FALLBACK DELIVERY TIME
                     */

                    if (dto.getDistanceKm() != null && dto.getDistanceKm() > 0) {

                        int travelMins = (int) Math.ceil((dto.getDistanceKm() / 20.0) * 60);

                        dto.setDeliveryTime((5 + travelMins) + " mins");

                    } else {

                        dto.setDeliveryTime("10 mins");
                    }
                }

            } else {

                /*
                 * LAT LNG NOT AVAILABLE
                 */

                dto.setRoadDistance(null);

                if (dto.getDistanceKm() != null && dto.getDistanceKm() > 0) {

                    int travelMins = (int) Math.ceil((dto.getDistanceKm() / 20.0) * 60);

                    dto.setDeliveryTime((5 + travelMins) + " mins");

                } else {

                    dto.setDeliveryTime("10 mins");
                }
            }

            /*
             * =========================================================================
             * ATTACH DISCOUNT INFO TO DTO (PRIORITY MATCHING)
             * =========================================================================
             */
            if (outletId != null) {

                // PRIORITY 1: Merchant Promotion
                if (merchantPromotionsMap.containsKey(outletId)) {
                    List<FmActivePromotionDiscountsProjection> promos = merchantPromotionsMap.get(outletId);

                    // Set offer badge text (e.g., "FLAT ₹80 OFF" or "20% OFF")
                    FmActivePromotionDiscountsProjection promotionDiscountsProjection = promos.get(0);

                    FmActiveDiscountsDto activeDiscountsDto = new FmActiveDiscountsDto();

                    // Set discounted details on product DTO
                    activeDiscountsDto.setPlanType(promotionDiscountsProjection.getPlanType());
                    activeDiscountsDto.setDiscountAmount(promotionDiscountsProjection.getOfferAmount());
                    activeDiscountsDto.setMinOrderValue(promotionDiscountsProjection.getMinimumOrderValue());
                    activeDiscountsDto.setOfferName(promotionDiscountsProjection.getOfferName());
                    activeDiscountsDto.setPriceType(promotionDiscountsProjection.getOfferType());

                    dto.setActiveDiscountsDto(activeDiscountsDto);

                    // PRIORITY 2: Division Coupon / Price Drop
                } else if (divisionDiscountsMap.containsKey(outletId)) {
                    List<FmActiveDiscountsResponseDto> divisionDiscounts = divisionDiscountsMap.get(outletId);
                    FmActiveDiscountsResponseDto activeDiscounts = divisionDiscounts.get(0);

                    FmActiveDiscountsDto activeDiscountsDto = FmOutletMapper.mapToActiveDiscounts(activeDiscounts);
                    dto.setActiveDiscountsDto(activeDiscountsDto);
                }
            }

            outlets.add(dto);
        }

        /*
         * SUCCESS RESPONSE
         */

        FmCustomerNearbyResponseDto response = new FmCustomerNearbyResponseDto();

        response.setCustomerLat(customerLat);

        response.setCustomerLng(customerLng);

        response.setRadiusKm(radiusKm);

        response.setTotalOutlets(outlets.size());

        response.setMessage("Nearby outlets fetched successfully");

        response.setOutlets(outlets);

        return response;
    }

    @Override
    public FmPublicCustomerNearbyResponseDto fetchPublicCustomerNearbyOutlets(double customerLat, double customerLng) {

        double radiusKm = 10.0;

        log.info("[OutletService] fetchPublicCustomerNearbyOutlets lat={} lng={} radius={} km", customerLat, customerLng, radiusKm);

        List<FmPublicCustomerNearbyOutletProjection> projections = outletRepository.fetchPublicCustomerNearbyOutlets(customerLat, customerLng);

        /*
         * NO OUTLETS FOUND
         */
        if (projections.isEmpty()) {

            FmPublicCustomerNearbyResponseDto response = new FmPublicCustomerNearbyResponseDto();

            response.setCustomerLat(customerLat);

            response.setCustomerLng(customerLng);

            response.setRadiusKm(radiusKm);

            response.setTotalOutlets(0);

            response.setMessage("Service is not available in this area");

            response.setOutlets(List.of());

            return response;
        }

        /*
         * MAP PROJECTIONS TO DTOs
         */
        List<FmPublicCustomerNearbyOutletDto> outlets = projections.stream()
                .map(projection -> {
                    FmPublicCustomerNearbyOutletDto dto = new FmPublicCustomerNearbyOutletDto();
                    dto.setOutletId(projection.getOutletId());
                    dto.setOutletName(projection.getOutletName());
                    dto.setMerchantId(projection.getMerchantId());
                    dto.setRating(projection.getRating());
                    dto.setIsActive(projection.getIsActive());
                    dto.setIsApproved(projection.getIsApproved());
                    dto.setOpenNow(projection.getOpenNow());
                    dto.setIsVegOutlet(projection.getIsVegOutlet());
                    dto.setOutletPicUrl(projection.getOutletPicUrl());
                    return dto;
                })
                .collect(Collectors.toList());

        /*
         * SUCCESS RESPONSE
         */
        FmPublicCustomerNearbyResponseDto response = new FmPublicCustomerNearbyResponseDto();

        response.setCustomerLat(customerLat);

        response.setCustomerLng(customerLng);

        response.setRadiusKm(radiusKm);

        response.setTotalOutlets(outlets.size());

        response.setMessage("Nearby outlets fetched successfully");

        response.setOutlets(outlets);

        return response;
    }

    @Override
    public List<FmOutlet> getOutletsByAreaId(Integer areaId) {

        log.info("Fetching outlets by areaId={}", areaId);

        return outletRepository.getOutletsByAreaId(areaId);
    }

    @Override
    public OutletLocationResponseDto getOutletAddressDetails(Integer outletId) {

        // Use parameterized logging to avoid unnecessary String concatenation
        log.info("Fetching address details  for outletId: {}", outletId);

        OutletAddressProjection outletAddressProjection = outletRepository.getOutletAddressDetails(outletId);

        if (outletAddressProjection == null) {
            // Log the error locally before throwing to capture the failure context in the server logs
            log.error("Location retrieval failed: Outlet with ID {} does not exist or has no coordinates", outletId);

            throw new ResourceNotFoundException("Outlet not found with ID: " + outletId);
        }

        OutletLocationResponseDto response = new OutletLocationResponseDto();
        response.setOutletId(outletAddressProjection.getOutletId());
        response.setStateId(outletAddressProjection.getStateId());
        response.setCityId(outletAddressProjection.getCityId());
        response.setAreaId(outletAddressProjection.getAreaId());

        log.info("Successfully mapped address data for outletId: {}", outletId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public FmAdminOutletDetailsDto getAdminOutletDetails(Integer outletId) {

        log.info("ADMIN_OUTLET_DETAILS_START | outletId={}", outletId);

        // =========================================================
        // 1. VALIDATE OUTLET ID
        // =========================================================

        if (outletId == null || outletId <= 0) {

            log.warn("ADMIN_OUTLET_DETAILS_INVALID_ID | outletId={}", outletId);

            throw new IllegalArgumentException("Valid outletId is required.");
        }

        // =========================================================
        // 2. VALIDATE OUTLET EXISTS
        // =========================================================

        boolean exists = outletRepository.existsById(outletId);

        if (!exists) {

            log.warn("ADMIN_OUTLET_DETAILS_NOT_FOUND | outletId={}", outletId);

            throw new ResourceNotFoundException("Outlet not found with id: " + outletId);
        }

        // =========================================================
        // 3. FETCH COMPLETE OUTLET MENU DATA
        // =========================================================

        List<FmAdminOutletMenuProjection> rows = outletRepository.getAdminOutletMenu(outletId);

        if (rows == null || rows.isEmpty()) {

            log.warn("ADMIN_OUTLET_DETAILS_NO_DATA | outletId={}", outletId);

            throw new ResourceNotFoundException("No outlet details found for outletId: " + outletId);
        }

        // =========================================================
        // 4. MAP PROJECTION TO DTO
        // =========================================================

        FmAdminOutletDetailsDto response = FmOutletMapper.mapAdminOutletToDto(rows);

        // =========================================================
        // 5. KYC DETAILS
        // =========================================================

        FmUserKyc kyc = userKycRepository.findByEntityIdAndEntityType(outletId, FmAppConstants.TYPE_OUTLET).orElse(null);

        if (kyc != null) {

            response.setFssaiNumber(kyc.getFssaiNumber());

            response.setGstNumber(kyc.getGstNumber());
        }

        // =========================================================
        // 6. OUTLET DETAILS NOT IN PROJECTION
        // =========================================================

        FmOutlet outlet = outletRepository.findById(outletId).orElseThrow(() -> new ResourceNotFoundException("Outlet not found with id: " + outletId));

        // =========================================================
        // 7. OUTLET STATUS
        // =========================================================
        response.setIsActive(outlet.getIsActive());

        response.setIsApproved(Boolean.TRUE.equals(outlet.getIsApproved()));

        response.setIsAvailable(Boolean.TRUE.equals(outlet.getIsToggle()));

        response.setIsToggle(Boolean.TRUE.equals(outlet.getIsToggle()));

        response.setIsGstApplied(Boolean.TRUE.equals(outlet.getIsGstApplied()));

        // =========================================================
        // 8. OUTLET IMAGE
        // =========================================================

        response.setOutletPicUrl(outlet.getOutletPicUrl());

        // =========================================================
        // 9. LOG SUCCESS
        // =========================================================

        log.info("ADMIN_OUTLET_DETAILS_SUCCESS | outletId={} | categories={} | products={}", outletId, response.getCategories() == null ? 0 : response.getCategories().size(), response.getCategories() == null ? 0 : response.getCategories().stream().mapToInt(category -> category.getProducts() == null ? 0 : category.getProducts().size()).sum());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public FmPublicOutletDetailsDto getPublicOutletDetails(Integer outletId) {

        log.info("PUBLIC_OUTLET_DETAILS_START | outletId={}", outletId);

        // =========================================================
        // 1. VALIDATE OUTLET ID
        // =========================================================

        if (outletId == null || outletId <= 0) {

            log.warn("PUBLIC_OUTLET_DETAILS_INVALID_ID | outletId={}", outletId);

            throw new BadRequestException("Invalid outlet ID");
        }

        // =========================================================
        // 2. FETCH DATA FROM REPOSITORY
        // =========================================================

        List<FmPublicOutletDetailsProjection> rows = outletRepository.getPublicOutletDetails(outletId);

        // =========================================================
        // 3. VALIDATE DATA EXISTS
        // =========================================================

        if (rows == null || rows.isEmpty()) {

            log.warn("PUBLIC_OUTLET_DETAILS_NO_DATA | outletId={}", outletId);

            throw new ResourceNotFoundException("No outlet details found for outletId: " + outletId);
        }

        // =========================================================
        // 4. MAP PROJECTION TO DTO
        // =========================================================

        FmPublicOutletDetailsDto response = FmPublicOutletMapper.mapPublicOutletToDto(rows);

        // =========================================================
        // 5. LOG SUCCESS
        // =========================================================

        log.info("PUBLIC_OUTLET_DETAILS_SUCCESS | outletId={} | categories={} | products={}", outletId, response.getCategories() == null ? 0 : response.getCategories().size(), response.getCategories() == null ? 0 : response.getCategories().stream().mapToInt(category -> category.getProducts() == null ? 0 : category.getProducts().size()).sum());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getAreaIdByOutletId(Integer outletId) {

        log.info("Fetching areaId for outletId={}", outletId);

        FmOutletAddress address = addressRepository.findByJippyAddressIdAndAddressType(outletId, FmAppConstants.TYPE_OUTLET).orElseThrow(() -> new ResourceNotFoundException("Outlet address not found for outletId: " + outletId));

        if (address.getAreaId() == null) {
            throw new ResourceNotFoundException("Area not configured for outletId: " + outletId);
        }

        return address.getAreaId();
    }
    @Override
    public FmResponseDto updateOutletProfilePic(
            FmUpdateOutletProfilePicDto outletDto) {

        log.info(
                "[OUTLET] Update profile picture API started. outletId={}",
                outletDto != null ? outletDto.getOutletId() : null
        );

        // ================================================================
        // 1. Validate request body
        // ================================================================

        if (outletDto == null) {

            log.error("[OUTLET] Request body is null");

            throw new IllegalArgumentException(
                    "Outlet details are required"
            );
        }

        // ================================================================
        // 2. Validate outlet ID
        // ================================================================

        if (outletDto.getOutletId() == null) {

            log.error("[OUTLET] Outlet ID is required");

            throw new IllegalArgumentException(
                    "Outlet ID is required"
            );
        }

        log.info(
                "[OUTLET] Outlet ID received: {}",
                outletDto.getOutletId()
        );

        // ================================================================
        // 3. Validate profile picture URL
        // ================================================================

        if (outletDto.getOutletPicUrl() == null ||
                outletDto.getOutletPicUrl().trim().isEmpty()) {

            log.error(
                    "[OUTLET] Profile picture URL is required. outletId={}",
                    outletDto.getOutletId()
            );

            throw new IllegalArgumentException(
                    "Profile picture URL is required"
            );
        }

        // ================================================================
        // 4. Find outlet
        // ================================================================

        FmOutlet outlet = outletRepository
                .findById(outletDto.getOutletId())
                .orElseThrow(() -> {

                    log.error(
                            "[OUTLET] Outlet not found. outletId={}",
                            outletDto.getOutletId()
                    );

                    return new ResourceNotFoundException(
                            "Outlet not found with ID: "
                                    + outletDto.getOutletId()
                    );
                });

        // ================================================================
        // 5. Update profile picture
        // ================================================================

        outlet.setOutletPicUrl(
                outletDto.getOutletPicUrl()
        );

        // ================================================================
        // 6. Save
        // ================================================================

        outletRepository.save(outlet);

        log.info(
                "[OUTLET] Profile picture updated successfully. outletId={}",
                outlet.getOutletId()
        );

        // ================================================================
        // 7. Return response
        // ================================================================

        return new FmResponseDto(
                "200",
                "Outlet profile picture updated successfully"
        );
    }
//    ==============================================================================
//    ==============================================================================
    // ================================================================
// TOGGLE OUTLET
// ================================================================
//
// This method updates the is_toggle column of the outlet.
//
// Example:
//
// outletId = 132
// isToggle = true
//
// Result:
//
// outlets
// outlet_id | is_toggle
// ----------+----------
// 132       | true
//
// ================================================================

    @Override
    @Transactional
    public FmResponseDto toggleForOutlet(
            FmToggleOutletRequestDto requestDto) {

        log.info(
                "[OUTLET TOGGLE] Toggle API started. outletId={}, isToggle={}",
                requestDto != null ? requestDto.getOutletId() : null,
                requestDto != null ? requestDto.getIsToggle() : null
        );

        // ============================================================
        // 1. Validate request body
        // ============================================================

        if (requestDto == null) {

            log.error(
                    "[OUTLET TOGGLE] Request body is null"
            );

            throw new IllegalArgumentException(
                    "Outlet toggle details are required"
            );
        }

        // ============================================================
        // 2. Validate outlet ID
        // ============================================================

        if (requestDto.getOutletId() == null) {

            log.error(
                    "[OUTLET TOGGLE] Outlet ID is required"
            );

            throw new IllegalArgumentException(
                    "Outlet ID is required"
            );
        }

        // ============================================================
        // 3. Validate isToggle
        // ============================================================

        if (requestDto.getIsToggle() == null) {

            log.error(
                    "[OUTLET TOGGLE] isToggle value is required. outletId={}",
                    requestDto.getOutletId()
            );

            throw new IllegalArgumentException(
                    "isToggle value is required"
            );
        }

        log.info(
                "[OUTLET TOGGLE] Updating outlet. outletId={}, isToggle={}",
                requestDto.getOutletId(),
                requestDto.getIsToggle()
        );

        // ============================================================
        // 4. Check whether outlet exists
        // ============================================================

        boolean outletExists = outletRepository.existsById(
                        requestDto.getOutletId()
                );

        if (!outletExists) {

            log.error(
                    "[OUTLET TOGGLE] Outlet not found. outletId={}",
                    requestDto.getOutletId()
            );

            throw new ResourceNotFoundException(
                    "Outlet not found with ID: "
                            + requestDto.getOutletId()
            );
        }

        // ============================================================
        // 5. Update is_toggle column
        // ============================================================

        int updatedRows =
                outletRepository.updateOutletToggle(
                        requestDto.getOutletId(),
                        requestDto.getIsToggle()
                );

        // ============================================================
        // 6. Validate update
        // ============================================================

        if (updatedRows == 0) {

            log.error(
                    "[OUTLET TOGGLE] Failed to update outlet. outletId={}",
                    requestDto.getOutletId()
            );

            throw new RuntimeException(
                    "Failed to update outlet toggle"
            );
        }

        // ============================================================
        // 7. Log successful update
        // ============================================================

        log.info(
                "[OUTLET TOGGLE] Outlet toggle updated successfully. " +
                        "outletId={}, isToggle={}",
                requestDto.getOutletId(),
                requestDto.getIsToggle()
        );

        // ============================================================
        // 8. Return success response
        // ============================================================

        String message =
                Boolean.TRUE.equals(requestDto.getIsToggle())
                        ? "Outlet toggle enabled successfully"
                        : "Outlet toggle disabled successfully";

        return new FmResponseDto("200", message);
    }
}

