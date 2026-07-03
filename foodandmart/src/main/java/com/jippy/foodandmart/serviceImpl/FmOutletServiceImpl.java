package com.jippy.foodandmart.serviceImpl;


import com.jippy.foodandmart.dto.FmCustomerNearbyResponseDto;
import com.jippy.division.dto.FmNearbyOutletDto;
import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmNearbyOutletMapper;
import com.jippy.foodandmart.mapper.FmOutletMapper;
import com.jippy.foodandmart.mapper.FmMerchantMapper;
import com.jippy.foodandmart.projections.FmOutletByMerchantProjection;
import com.jippy.foodandmart.projections.FmOutletMenuProjection;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.FmGoogleMapsService;
import com.jippy.foodandmart.service.IFmOutletService;
import com.jippy.foodandmart.util.FmCredentialUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
            FmOutletAddress addr = addressRepository.findByJippyAddressId(o.getOutletId()).orElse(null);
            result.add(FmOutletSummaryDTO.from(o, 0, addr));
        }
        return result;
    }

    @Override
    public List<FmOutletSummaryDTO> getOutletsByMerchantId(Integer merchantId) {
        List<FmOutlet> outlets = outletRepository.findByMerchantId(merchantId);
        List<FmOutletSummaryDTO> result = new ArrayList<>();
        for (FmOutlet o : outlets) {
            FmOutletAddress addr = addressRepository.findByJippyAddressId(o.getOutletId()).orElse(null);
            result.add(FmOutletSummaryDTO.from(o, 0, addr));
        }
        return result;
    }

//    @Override
//    public FmOutlet getOutletById(Integer id) {
//        return outletRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException
//                        ("Outlet ID " + id + " does not exist"));
//    }

//    -----------------------------------------------
    @Override
    public FmOutletResponseDto getOutletById(Integer outletId) {

        log.info("Fetching outlet details for outletId: {}", outletId);

        FmOutlet outlet = outletRepository.findById(outletId)
                .orElseThrow(() -> {
                    log.error("Outlet not found with outletId: {}", outletId);
                    return new ResourceNotFoundException("Outlet not found with id: " + outletId);
                });

        FmOutletResponseDto outletResponseDto = FmOutletMapper.toOutletResponseDto(outlet);
        log.info("Successfully fetched outlet details for outletId: {}", outletId);

        return outletResponseDto;

    }

     @Override
    public FmOutletCreatedDTO createOutlet(FmOutletRequestDTO dto) {
        return null;
    }
//    ------------------------------------------------------------------------
    // ── Single Create ─────────────────────────────────────────────────────────
    // @Override
    // @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    // public FmOutletCreatedDTO createOutlet(FmOutletRequestDTO dto) {

    //     log.info("[OUTLET] Creating outlet: name={}, merchantId={}, phone={}",
    //             dto.getOutletName(),
    //             dto.getMerchantId(),
    //             dto.getOutletPhone());

    //     validateOutletRequest(dto);

    //     if (!merchantRepository.existsById(dto.getMerchantId())) {
    //         throw new IllegalArgumentException(
    //                 "Merchant ID " + dto.getMerchantId() + " does not exist");
    //     }

    //     if (outletRepository.existsByOutletPhone(dto.getOutletPhone())) {
    //         throw new IllegalArgumentException(
    //                 "An outlet with phone " + dto.getOutletPhone() + " already exists");
    //     }

    //     if (userRepository.findByUsernameAndUserType(
    //             dto.getUsername(),
    //             FmAppConstants.TYPE_OUTLET
    //     ).isPresent()) {

    //         throw new IllegalArgumentException(
    //                 "Username already exists.");
    //     }

    //     if (outletRepository.existsByMerchantIdAndOutletName(
    //             dto.getMerchantId(),
    //             dto.getOutletName())) {

    //         throw new IllegalArgumentException(
    //                 "Outlet '" + dto.getOutletName() + "' already exists for this merchant");
    //     }

    //     Point location = buildPoint(
    //             dto.getLatitude(),
    //             dto.getLongitude());

    //     FmMerchant merchant = merchantRepository.findById(dto.getMerchantId())
    //             .orElseThrow(() ->
    //                     new ResourceNotFoundException("Merchant not found."));

    //     // Merchant email and outlet email must be same
    //     if (!merchant.getMerchantEmail().equalsIgnoreCase(dto.getEmail())) {

    //         throw new IllegalArgumentException(
    //                 "Merchant email and outlet email must be same."
    //         );
    //     }

    //     // Verify latest CREATE_OUTLET OTP
    //     FmEmailOtpVerification otpVerification =
    //             otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(
    //                             merchant.getMerchantEmail(),
    //                             FmOtpPurpose.CREATE_OUTLET)
    //                     .orElseThrow(() ->
    //                             new InvalidOtpException(
    //                                     "Please verify OTP before creating outlet."
    //                             ));

    //     if (otpVerification.getStatus() != FmOtpStatus.VERIFIED
    //             || !Boolean.TRUE.equals(otpVerification.getIsVerified())) {

    //         throw new InvalidOtpException(
    //                 "Please verify OTP before creating outlet."
    //         );
    //     }


    //     FmOutlet outlet = FmOutletMapper.toEntity(dto);

    //     // Always use merchant email
    //     outlet.setOutletEmail(
    //             merchant.getMerchantEmail()
    //     );

    //     outlet.setOutletLocation(location);

    //     outlet = outletRepository.save(outlet);

    //     log.info("[OUTLET] Saved: outletId={}", outlet.getOutletId());

    //     saveAddress(dto, outlet.getOutletId());

    //     saveOperatingDays(dto, outlet.getOutletId());

    //     saveOutletUser(
    //             dto.getUsername(),
    //             dto.getPassword(),
    //             outlet.getOutletId()
    //     );

    //     otpVerification.setStatus(FmOtpStatus.CONSUMED);
    //     otpVerification.setVerifiedAt(LocalDateTime.now());
    //     otpRepository.save(otpVerification);

    //     log.info("[OUTLET] Onboarding complete: outletId={}",
    //             outlet.getOutletId());

    //     return FmOutletMapper.toCreatedDTO(outlet);
    // }
    // ── Bulk Upload ───────────────────────────────────────────────────────────
   /* @Transactional(rollbackFor =  Exception.class)
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

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateOutletRequest(FmOutletRequestDTO dto) {
        List<String> errs = new ArrayList<>();

        if (isBlank(dto.getOutletName())) errs.add("Outlet name is required");
        else if (dto.getOutletName().length() > 100) errs.add("Outlet name must not exceed 100 characters");

        if (dto.getMerchantId() == null) errs.add("Merchant ID is required");

        if (isBlank(dto.getCuisineType())) errs.add("Cuisine type is required");
        else if (dto.getCuisineType().length() > 100) errs.add("Cuisine type must not exceed 100 characters");

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

    private void saveOperatingDays(FmOutletRequestDTO dto, Integer outletId) {
        if (dto.getOperatingDays() == null || dto.getOperatingDays().isEmpty()) return;
        for (FmOutletDayDTO d : dto.getOperatingDays()) {
            if (d.getDayOfWeekId() == null) continue;
            boolean isEvening = "evening".equalsIgnoreCase(d.getSlotType());
            LocalTime defOpen = isEvening ? LocalTime.of(17, 0) : LocalTime.of(9, 0);
            LocalTime defClose = isEvening ? LocalTime.of(22, 0) : LocalTime.of(14, 0);

            FmOutletDay day = new FmOutletDay();
            day.setOutletId(outletId);
            day.setDayOfWeekId(d.getDayOfWeekId());
            day.setIsOpen(d.getIsOpen() != null ? d.getIsOpen() : true);
            day.setOpeningTime(parseTime(d.getOpeningTime(), defOpen));
            day.setClosingTime(parseTime(d.getClosingTime(), defClose));
            dayRepository.save(day);
        }
        log.info("[OUTLET] Operating slots saved for outletId={}", outletId);
    }

    private void saveOutletUser(
            String username,
            String password,
            Integer outletId) {

        Optional<FmUser> existingUser =
                userRepository.findByUsernameAndUserType(
                        username,
                        FmAppConstants.TYPE_OUTLET
                );

        if (existingUser.isPresent()) {

            throw new ResourceNotFoundException(
                    "Username already exists."
            );
        }

        String encodedPassword =
                passwordEncoder.encode(password);

        FmUser user =
                FmMerchantMapper.toUserEntity(
                        username,
                        encodedPassword,
                        outletId,
                        FmAppConstants.TYPE_OUTLET
                );

        user = userRepository.save(user);

        log.info("[OUTLET] User created successfully. Username={}",
                username);

        FmRoles role =
                roleRepository.findByRoleName(
                        FmAppConstants.ROLE_OUTLET
                );

        if (role == null) {
            throw new RuntimeException("Role not found");
        }

        List<FmRolePermissions> permissions =
                rolePermissionsRepository.findByRole(role);

        if (permissions.isEmpty()) {
            throw new RuntimeException("No permissions mapped to role");
        }

        for (FmRolePermissions permission : permissions) {

            FmUserRolePermissions userRole =
                    FmMerchantMapper.toUserRolesEntity(
                            user,
                            permission
                    );

            userRolesRepository.save(userRole);
        }

        log.info("[OUTLET] Role mapping completed for username={}",
                username);
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
    public FmOutletDetailsDto getOutletDetails(Integer outletId, String userType,Integer customerId) {

        log.info("Fetching outlet details for outletId={}, userType={}, customerId={}",
                outletId, userType, customerId);

        List<FmOutletMenuProjection> rows = outletRepository.getOutletMenu(outletId);

        if (rows == null || rows.isEmpty()) {
            log.error("No data found for outletId={}", outletId);
            throw new ResourceNotFoundException("Outlet not found with id: " + outletId);
        }

        FmOutletDetailsDto outletDtoresponse = FmOutletMapper.mapToOutletDto(rows, userType);
        log.debug("Added For UI - Outlet availability : {}", outletDtoresponse.getIsAvailable());
//      -----------------------------------------------------------------------------------
        /*
         * Default value.
         * If customerId is not passed,
         * favourite should be false.
         */
        outletDtoresponse.setIsFavourite(false);

//        Check favourite only for CUSTOMER.
        if (FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType)
                && customerId != null) {

      log.info("Checking favourite status for customerId={} and outletId={}", customerId, outletId);

//      ----------------------------------------------------------------------------
     Optional<FmFavoriteOutlet> favourite =
                    favoriteOutletRepository.findByCustomerIdAndFavoriteIdAndFavouriteType(
                            customerId, outletId, FmAppConstants.TYPE_OUTLET);

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

    /** HELPER_METHOD 1
     * Populate favourite status for every product
     * available in the outlet.
     */
    private void setProductFavouriteStatus(
            FmOutletDetailsDto outlet, Integer customerId) {

        for (FmCategoryDto category : outlet.getCategories()) {

            for (FmProductDto product : category.getProducts()) {

                log.info("Checking Product Id : {}", product.getProductId());

                Optional<FmFavoriteOutlet> favourite =
                        favoriteOutletRepository
                                .findByCustomerIdAndFavoriteIdAndFavouriteType(
                                        customerId,
                                        product.getProductId(),
                                        FmAppConstants.TYPE_PRODUCT);

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

        if (rows == null || rows.isEmpty()) {
            throw new ResourceNotFoundException("No outlets found for merchantId: " + merchantId);
        }

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
                                if (vDto.getPrice() != null) variant.setMerchantPrice(vDto.getPrice());
                            }
                        }
                        // Update product price if provided
                        if (productDto.getPrice() != null) product.setMerchantPrice(productDto.getPrice());

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
        return getOutletDetails(outletId, userType,null);
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
    public FmAddressRequestDto saveAddressDetails(FmAddressRequestDto fmAddressRequestDto) {
        FmOutletAddress address = FmOutletMapper.toAddressEntity(fmAddressRequestDto);
        FmOutletAddress fmAddress = addressRepository.save(address);
        FmAddressRequestDto responseDto = FmOutletMapper.toAddressRequestDto(fmAddress);
        log.info("address saved for driver ID  ={}", address.getAddressId());

        return fmAddressRequestDto;
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
    public FmCustomerNearbyResponseDto fetchCustomerNearbyOutlets(double customerLat, double customerLng,Integer categoryId) {

        double radiusKm = FmAppConstants.DEFAULT_RADIUS_KM;

        log.info("[OutletService] fetchCustomerNearbyOutlets lat={} lng={} radius={} km", customerLat, customerLng, radiusKm);

        List<Object[]> rows = outletRepository.findCustomerNearbyOutlets(customerLat, customerLng,categoryId);

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

        List<FmNearbyOutletDto> outlets = new ArrayList<>();

        for (Object[] row : rows) {

            FmNearbyOutletDto dto = new FmNearbyOutletDto();

            dto.setOutletId(row[0] != null ? ((Number) row[0]).intValue() : null);

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
    public List<FmOutlet> getOutletsByAreaId(
            Integer areaId) {

        log.info(
                "Fetching outlets by areaId={}",
                areaId);

        return outletRepository
                .getOutletsByAreaId(areaId);
    }
}
