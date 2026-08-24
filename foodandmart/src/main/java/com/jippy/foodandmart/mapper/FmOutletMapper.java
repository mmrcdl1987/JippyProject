//package com.jippy.foodandmart.mapper;
//
//import com.jippy.foodandmart.dto.FmOutletCreatedDTO;
//import com.jippy.foodandmart.dto.FmOutletRequestDTO;
//import com.jippy.foodandmart.entity.FmOutlet;
//import com.jippy.foodandmart.entity.FmOutletAddress;
//
/// **
// * Static utility class for converting between {@link FmOutletRequestDTO} /
// * {@link FmOutletCreatedDTO} and the {@link FmOutlet} / {@link FmOutletAddress} entities.
// *
// * <p>Why a separate mapper: keeps the service layer clean by moving all
// * field-level mapping logic into one testable place. The service only calls
// * {@code OutletMapper.toEntity(dto)} rather than manually setting each field.</p>
// */
//public final class OutletMapper {
//
//    /**
//     * Private constructor — static utility class, must not be instantiated.
//     */
//    private OutletMapper() {}
//
//    // ── DTO → Entity ──────────────────────────────────────────────────────────
//
//    /**
//     * Converts an {@link FmOutletRequestDTO} into a new {@link FmOutlet} entity.
//     *
//     * <p>Why we use this: the Outlet entity has many fields and using setters
//     * here prevents the service from growing field-by-field assignment blocks.
//     * isActive defaults to "Y" because newly created outlets are immediately
//     * available for menu and product operations.</p>
//     *
//     * @param dto the validated inbound outlet creation request
//     * @return a transient {@link FmOutlet} entity (not yet persisted)
//     */
//    public static FmOutlet toEntity(FmOutletRequestDTO dto) {
//        FmOutlet outlet = new FmOutlet();
//        // Trim whitespace from user-supplied text fields
//        outlet.setOutletName(dto.getOutletName().trim());
//        outlet.setMerchantId(dto.getMerchantId());
//        outlet.setCuisineType(dto.getCuisineType().trim());
//        outlet.setOutletPhone(dto.getOutletPhone().trim());
//        // Every new outlet starts as active
//        outlet.setIsActive("Y");
//        return outlet;
//    }
//
//    /**
//     * Builds an {@link FmOutletAddress} entity from address fields in the DTO.
//     *
//     * <p>Why the stateId is a parameter: the service resolves the state name
//     * (a string) to the actual FK integer by querying the states table. This
//     * mapper only handles the field mapping, not that DB lookup.</p>
//     *
//     * <p>The {@code jippyAddressId} mirrors outletId — it acts as the Jippy
//     * platform's internal address identifier and is always the same as the
//     * outlet's PK in this design.</p>
//     *
//     * @param dto      the request DTO containing address fields
//     * @param outletId the PK of the newly saved outlet
//     * @param stateId  the resolved integer FK for the state name in the DTO
//     * @return a transient {@link FmOutletAddress} entity ready to persist
//     */
//    /**
//     * Builds an {@link FmOutletAddress} from the DTO plus the two resolved integer FKs.
//     *
//     * @param dto      the request DTO containing address fields
//     * @param outletId the PK of the newly saved outlet
//     * @param stateId  the resolved integer FK for the state name
//     * @param areaId   the resolved integer FK for the area name (from ZipCode column)
//     * @return a transient {@link FmOutletAddress} entity ready to persist
//     */
//    public static FmOutletAddress toAddressEntity(FmOutletRequestDTO dto, Integer outletId,
//                                                  Integer stateId, Integer areaId) {
//        FmOutletAddress address = new FmOutletAddress();
//        // FK to outlets table
//        address.setOutletId(outletId);
//        // Jippy platform internal address ID mirrors the outlet PK
//        address.setJippyAddressId(outletId);
//        address.setBuildingNumber(safe(dto.getBuildingNumber()));
//        address.setRoad(safe(dto.getRoad()));
//        address.setLandmark(safe(dto.getLandmark()));
//        // cityId defaults to 0 when not provided to satisfy NOT NULL DB constraint
//        address.setCityId(dto.getCityId() != null ? dto.getCityId() : 0);
//        address.setStateId(stateId);
//        // area_id — resolved from the area name supplied in the ZipCode column
//        address.setAreaId(areaId);
//        address.setAddressType("OUTLET");
//        return address;
//    }
//
//    // ── Entity → DTO ──────────────────────────────────────────────────────────
//
//    /**
//     * Converts a saved {@link FmOutlet} entity into an {@link FmOutletCreatedDTO}.
//     *
//     * <p>Why we return a dedicated DTO instead of the entity: the response
//     * must include the auto-generated portal credentials (loginId and password)
//     * which are not stored on the entity for security reasons. The DTO carries
//     * these one-time values back to the caller.</p>
//     *
//     * @param outlet   the persisted outlet entity
//     * @param loginId  the auto-generated login ID (e.g. "ravi4567")
//     * @param password the auto-generated plain-text password shown once to the admin
//     * @return an {@link FmOutletCreatedDTO} containing entity fields plus credentials
//     */
//    public static FmOutletCreatedDTO toCreatedDTO(FmOutlet outlet, String loginId, String password) {
//        FmOutletCreatedDTO dto = new FmOutletCreatedDTO();
//        dto.setOutletId(outlet.getOutletId());
//        dto.setOutletName(outlet.getOutletName());
//        dto.setMerchantId(outlet.getMerchantId());
//        dto.setCuisineType(outlet.getCuisineType());
//        dto.setOutletPhone(outlet.getOutletPhone());
//        dto.setIsActive(outlet.getIsActive());
//        // Credentials are generated by CredentialUtil in the service layer and passed in here
//        dto.setOutletLoginId(loginId);
//        dto.setOutletPassword(password);
//        return dto;
//    }
//
//    /**
//     * Null-safe trimmer for address string fields.
//     *
//     * <p>Why we use this helper: several address fields are optional; returning
//     * an empty string instead of null avoids NullPointerExceptions downstream
//     * and satisfies the NOT NULL DB constraints on address columns.</p>
//     *
//     * @param s the raw string value, possibly null
//     * @return trimmed value, or empty string if null
//     */
//    private static String safe(String s) {
//        return s != null ? s.trim() : "";
//    }
//}


package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmOutletDto;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.entity.FmMerchantBankDetails;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmOutletAddress;
import com.jippy.foodandmart.entity.FmOutletDay;
import com.jippy.foodandmart.projections.FmMerchantOutletMenuProjection;
import com.jippy.foodandmart.projections.FmOutletByMerchantProjection;
import com.jippy.foodandmart.projections.FmOutletMenuProjection;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Static utility class for converting between Entities, DTOs, and Projections
 * related to Outlets, Menus, and Addresses.
 */
@Slf4j
public final class FmOutletMapper {

    public static FmNearbyOutletMapper NearbyOutletMapper;

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private FmOutletMapper() {
    }

    // ── DTO → Entity (Outlet Creation) ────────────────────────────────────────

    /**
     * Converts an {@link FmOutletRequestDTO} into a new {@link FmOutlet} entity.
     */
    public static FmOutlet toEntity(FmOutletRequestDTO dto) {

        FmOutlet outlet = new FmOutlet();

        outlet.setOutletName(dto.getOutletName().trim());
        outlet.setMerchantId(dto.getMerchantId());
        outlet.setCuisineType(dto.getCuisineType());
        outlet.setOutletPhone(dto.getOutletPhone().trim());
        outlet.setOutletEmail(dto.getOutletEmail());
        outlet.setAlternateOutletPhone(dto.getAlternateOutletPhone());
        outlet.setIsActive(FmAppConstants.FLAG_YES);
        outlet.setIsApproved(FmAppConstants.STATUS_FALSE);
        outlet.setUpdatedBy(dto.getUpdatedBy());

        return outlet;
    }


    public static FmOutletResponseDto toOutletResponseDto(FmOutlet outlet) {

        FmOutletResponseDto dto = new FmOutletResponseDto();

        dto.setOutletId(outlet.getOutletId());
        dto.setOutletName(outlet.getOutletName());
        dto.setOutletEmail(outlet.getOutletEmail());
        dto.setMerchantId(outlet.getMerchantId());
        dto.setCuisineType(outlet.getCuisineType());
        dto.setOutletPhone(outlet.getOutletPhone());
        dto.setRadius(outlet.getRadius());
        dto.setIsActive(outlet.getIsActive());
        dto.setIsApproved(outlet.getIsApproved());
        dto.setOutletPicUrl(outlet.getOutletPicUrl());

        // Convert Point -> Latitude X & Longitude Y
        if (outlet.getOutletLocation() != null) {
            dto.setLongitude(outlet.getOutletLocation().getX());
            dto.setLatitude(outlet.getOutletLocation().getY());
        }

        return dto;
    }

    public static FmAddressRequestDto toAddressRequestDto(FmOutletAddress fmOutletAddress) {
        FmAddressRequestDto fmAddressRequestDto = new FmAddressRequestDto();
        fmAddressRequestDto.setJippyAddressId(fmOutletAddress.getJippyAddressId());
        fmAddressRequestDto.setBuildingNumber(fmOutletAddress.getBuildingNumber());
        fmAddressRequestDto.setRoad(fmOutletAddress.getRoad());
        fmAddressRequestDto.setLandmark(fmOutletAddress.getLandmark());
        fmAddressRequestDto.setCityId(fmOutletAddress.getCityId());
        fmAddressRequestDto.setStateId(fmOutletAddress.getStateId());
        fmAddressRequestDto.setAreaId(fmOutletAddress.getAreaId());
        fmAddressRequestDto.setAddressType(fmOutletAddress.getAddressType());
        return fmAddressRequestDto;
    }
    // ADDRESS -> OUTLET RESPONSE DTO
    public static void mapAddressToOutletResponse(FmOutletResponseDto response, FmOutletAddress address) {

        if (response == null || address == null) {
            return;
        }

        response.setBuildingNumber(address.getBuildingNumber());

        response.setRoad(address.getRoad());

        response.setLandmark(address.getLandmark());

    }

    // BANK DETAILS -> OUTLET RESPONSE DTO
    public static void mapBankDetailsToOutletResponse(FmOutletResponseDto response, FmMerchantBankDetails bankDetails) {

        if (response == null || bankDetails == null) {
            return;
        }

        response.setAccountNumber(bankDetails.getAccountNumber());

        response.setIfscCode(bankDetails.getIfscCode());

        response.setBankName(bankDetails.getBankName());

        response.setAccountHolderName(bankDetails.getAccountHolderName());
    }
    //    for feign client to map the address details from Outletdto to AddressRequestdto to send to address microservice
    public static FmAddressRequestDto convertToAddressReqDto(FmOutletRequestDTO dto, Integer outletId, Integer stateId, Integer areaId) {
        FmAddressRequestDto fmAddressRequestDto = new FmAddressRequestDto();
        fmAddressRequestDto.setAddressType(FmAppConstants.TYPE_OUTLET);
        fmAddressRequestDto.setJippyAddressId(outletId);
        fmAddressRequestDto.setBuildingNumber(dto.getBuildingNumber());
        fmAddressRequestDto.setRoad(dto.getRoad());
        fmAddressRequestDto.setLandmark(dto.getLandmark());
        fmAddressRequestDto.setCityId(dto.getCityId() != null ? dto.getCityId() : 0);
        fmAddressRequestDto.setStateId(stateId);
        fmAddressRequestDto.setAreaId(areaId);
        return fmAddressRequestDto;
    }
// OUTLET DAYS -> OUTLET RESPONSE DTO

    public static void mapOperatingDaysToOutletResponse(FmOutletResponseDto response, List<FmOutletDay> outletDays) {

        if (response == null) {
            return;
        }

        if (outletDays == null || outletDays.isEmpty()) {
            response.setOperatingDays(new ArrayList<>());
            return;
        }

        response.setOperatingDays(outletDays.stream().map(FmOutletDayMapper::toDTO).toList());
    }
    /**
     * Builds an {@link FmOutletAddress} from the DTO plus resolved integer FKs.
     */


    public static FmOutletAddress toAddressEntity(FmAddressRequestDto dto) {

        FmOutletAddress address = new FmOutletAddress();
        address.setJippyAddressId(dto.getJippyAddressId());
        address.setBuildingNumber(safe(dto.getBuildingNumber()));
        address.setRoad(safe(dto.getRoad()));
        address.setLandmark(safe(dto.getLandmark()));
        address.setCityId(dto.getCityId() != null ? dto.getCityId() : 0);
        address.setStateId(dto.getStateId());
        address.setAreaId(dto.getAreaId());
        address.setAddressType(dto.getAddressType());
        return address;
    }

    // ── Entity → DTO ──────────────────────────────────────────────────────────

    /**
     * Converts a saved {@link FmOutlet} entity into an {@link FmOutletCreatedDTO}
     * including one-time generated credentials.
     */
    public static FmOutletCreatedDTO toCreatedDTO(FmOutlet outlet) {

        FmOutletCreatedDTO dto = new FmOutletCreatedDTO();

        dto.setOutletId(outlet.getOutletId());
        dto.setOutletName(outlet.getOutletName());
        dto.setMerchantId(outlet.getMerchantId());
        dto.setCuisineType(outlet.getCuisineType());
        dto.setOutletPhone(outlet.getOutletPhone());
        dto.setIsActive(outlet.getIsActive());

        return dto;
    }

    // ── Projection → DTO (Complex Menu Mapping) ───────────────────────────────


    /**
     * Maps flat database result sets (Projections) into a nested Outlet Hierarchy.
     */
    public static FmOutletDetailsDto mapCustomerToOutletDto(List<FmOutletMenuProjection> rows, String userType) {
        if (rows == null || rows.isEmpty()) {
            log.warn("No data found to map");
            return null;
        }

        log.debug("Mapping {} rows into OutletDetailsDto", rows.size());
        FmOutletDetailsDto outlet = new FmOutletDetailsDto();
        Map<String, FmOutletTimingDto> outletTimingMap = new LinkedHashMap<>();
        Map<Integer, FmCategoryDto> categoryMap = new LinkedHashMap<>();

        for (FmOutletMenuProjection row : rows) {
            if (row != null) {
                // Set basic outlet info (repeated in rows)
                outlet.setOutletId(row.getOutletId());
                outlet.setOutletName(row.getOutletName());
                outlet.setOutletPhone(row.getOutletPhone());
                outlet.setAlternateOutletPhone(row.getAlternateOutletPhone());
                outlet.setOutletEmail(row.getOutletEmail());

                /*
                 * Merchant users can view complete outlet configuration details.
                 * Customer userType should not receive these details. only visible
                 * to merchant userType
                 */
                if (FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(userType)) {

                    // Outlet Details
//                    outlet.setCuisineType(row.getCuisineType());
                    outlet.setLatitude(row.getLatitude());
                    outlet.setLongitude(row.getLongitude());

                    // Bank Details
                    outlet.setAccountNumber(row.getAccountNumber());
                    outlet.setIfscCode(row.getIfscCode());
                    outlet.setBankName(row.getBankName());
                    outlet.setAccountHolderName(row.getAccountHolderName());

                    // Address Details
                    outlet.setBuildingNumber(row.getBuildingNumber());
                    outlet.setRoad(row.getRoad());
                    outlet.setLandmark(row.getLandmark());

                    outlet.setCityId(row.getCityId());
                    outlet.setCityName(row.getCityName());

                    outlet.setStateId(row.getStateId());
                    outlet.setStateName(row.getStateName());

                    outlet.setAreaId(row.getAreaId());
                    outlet.setAreaName(row.getAreaName());
                }

                // Map Timings
                String day = row.getOutletDay();
                if (day != null && !outletTimingMap.containsKey(day)) {
                    FmOutletTimingDto timing = new FmOutletTimingDto();
                    timing.setDay(day);
                    timing.setIsOpen(row.getIsOpen());
                    timing.setOpeningTime(row.getOpeningTime());
                    timing.setClosingTime(row.getClosingTime());
                    outletTimingMap.put(day, timing);
                }

                // Map Categories
                Integer categoryId = row.getCategoryId();
                if (categoryId != null) {
                    FmCategoryDto category = categoryMap.computeIfAbsent(categoryId, id -> {
                        FmCategoryDto c = new FmCategoryDto();
                        c.setCategoryId(id);
                        c.setCategoryName(row.getCategoryName());
                        // Category availability from outlet_categories.is_toggle
                        c.setIsAvailable(row.getCategoryAvailable());
                        c.setProducts(new ArrayList<>());
                        return c;
                    });

                    // Map Products
                    if (row.getProductId() != null) {
                        FmProductDto product = category.getProducts().stream().filter(p -> p.getProductId().equals(row.getProductId())).findFirst().orElse(null);

                        if (product == null) {
                            product = new FmProductDto();
                            product.setProductId(row.getProductId());
                            product.setProductName(row.getProductName());
                            product.setDescription(row.getDescription());
                            // Product image
                            product.setImageLink(row.getImageLink());
                            // =========================================================
                            // Product Pricing
                            // =========================================================
                            //
                            // MERCHANT:
                            //     merchantPrice = products.merchant_price
                            //     price         = null
                            //
                            // CUSTOMER:
                            //     If online price exists:
                            //         merchantPrice = null
                            //         price = online price
                            //
                            //     If online price does NOT exist:
                            //         merchantPrice = merchant price
                            //         price = null
                            // =========================================================

                            if (FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(userType)) {

                                // Merchant gets merchant price
                                product.setMerchantPrice(row.getMerchantPrice());
                                product.setOnlinePrice(null);

                            } else if (FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType)) {

                                if (row.getOnlinePrice() != null) {

                                    // Customer gets online price
                                    product.setMerchantPrice(null);
                                    product.setOnlinePrice(row.getOnlinePrice());

                                } else {

                                    // No online price → fallback to merchant price
                                    product.setMerchantPrice(row.getMerchantPrice());
                                    product.setOnlinePrice(null);
                                }
                            }

                            product.setIsVeg(row.getIsVeg());
                            product.setHasProductVariants(row.getHasProductVariants());
                            // Product availability from products.is_toggle
                            product.setIsAvailable(row.getProductAvailable());
                            product.setProductTimings(new ArrayList<>());
                            product.setVariants(new ArrayList<>());
                            category.getProducts().add(product);
                        }

                        /* -----------------------------------------------
                         * Map Product Timings (Avoid Duplicate Timings)
                         */
                        if (row.getStartTime() != null) {

                            boolean timingExists = false;

                            for (FmProductTimingDto existingTiming : product.getProductTimings()) {

                                if (existingTiming.getDay().equals(row.getProductDay()) && existingTiming.getStartTime().equals(row.getStartTime()) && existingTiming.getEndTime().equals(row.getEndTime())) {

                                    timingExists = true;
                                    break;
                                }
                            }

                            if (!timingExists) {

                                FmProductTimingDto pt = new FmProductTimingDto();

                                pt.setDay(row.getProductDay());
                                pt.setStartTime(row.getStartTime());
                                pt.setEndTime(row.getEndTime());

                                product.getProductTimings().add(pt);
                            }
                        }
                        // =========================================================
                        // Map Product Variants
                        // =========================================================
                        //
                        // Only products with has_product_variants = true
                        // should contain variant information.
                        // =========================================================
                        /*
                         * Map Product Variants.
                         */
                if (Boolean.TRUE.equals(row.getHasProductVariants()) && row.getProductVariantId() != null) {

                    boolean variantExists = false;

                    // ---------------------------------------------------------
                    // Check whether this variant was already added
                    // ---------------------------------------------------------
                    for (FmProductVariantDTO existingVariant : product.getVariants()) {

                        if (existingVariant.getVariantId().equals(row.getProductVariantId())) {

                            variantExists = true;
                            break;
                        }
                    }

            // ---------------------------------------------------------
            // Add variant only if it does not already exist
            // ---------------------------------------------------------
                    if (!variantExists) {

                        FmProductVariantDTO variant = new FmProductVariantDTO();

            //                                variant.setVariantId(row.getProductVariantId());
            //                                variant.setVariantName(row.getVariantName());
            //
            //                                /*
            //                                 * Customer sees online price.
            //                                 * Merchant sees merchant price.
            //                                 */
            //                                if (FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType)
            //                                        && row.getOnlinePrice() != null) {
            //
            //                                    variant.setPrice(row.getOnlinePrice());
            //
            //                                } else {
            //
            //                                    variant.setMerchantPrice(row.getVariantMerchantPrice());
            //                                }
            //
            //                                product.getVariants().add(variant);


                // =========================================================
                // Variant Option
                // =========================================================

                variant.setVariantId(row.getProductVariantId());


                // =========================================================
                // Variant Group Value
                // =========================================================

//                commented for UI
//              variant.setVariantValueId(row.getVariantValueId());

                        variant.setVariantName(row.getVariantName());


                    // =========================================================
                    // Variant Group
                    // =========================================================

//                  commented for UI
//                  variant.setVariantGroupId(row.getVariantGroupId());

                    variant.setGroupName(row.getVariantGroupName());

//                  variant.setSelectionType(row.getVariantSelectionType());

//                   commented for UI
//                  variant.setMinSelection(row.getVariantMinSelection());

//                  variant.setMaxSelection(row.getVariantMaxSelection());


                        // =========================================================
                        // Price Type
                        // MAIN / ADD
                        // =========================================================

                        variant.setPriceType(row.getVariantPriceType());

                        // =========================================================
                        // Variant Pricing
                        // =========================================================
                        //
                        // MERCHANT:
                        //     merchantPrice = variant_price
                        //     price         = null
                        //
                        // CUSTOMER:
                        //     If variant online price exists:
                        //         merchantPrice = null
                        //         price = variant online price
                        //
                        //     If variant online price does NOT exist:
                        //         merchantPrice = variant merchant price
                        //         price = null
                        // =========================================================

                        if (FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(userType)) {

                            // Merchant gets variant merchant price
                            variant.setMerchantPrice(row.getVariantMerchantPrice());
                            variant.setOnlinePrice(null);

            } else if (FmAppConstants.TYPE_CUSTOMER.equalsIgnoreCase(userType)) {

                if (row.getVariantOnlinePrice() != null) {

                    // Customer gets variant online price
                    variant.setMerchantPrice(null);
                    variant.setOnlinePrice(row.getVariantOnlinePrice());

                } else {

                    // No variant online price → fallback to merchant price
                    variant.setMerchantPrice(row.getVariantMerchantPrice());
                    variant.setOnlinePrice(null);
                }
            }
                    // =========================================================
                    // Add to product
                    // =========================================================

                    product.getVariants().add(variant);
                            }
                        }
                    }
                }
            }
        }

        // =========================================================
        // FINAL OUTLET RESPONSE
        // =========================================================

        outlet.setOutletTimings(
                new ArrayList<>(outletTimingMap.values())
        );

        outlet.setCategories(
                new ArrayList<>(categoryMap.values())
        );

        return outlet;
    }

    //    ==============================================================================================
//    ==============================================================================================
    public static FmOutletDetailsDto mapMerchantToOutletDto(List<FmMerchantOutletMenuProjection> rows) {

        log.info("MERCHANT_OUTLET_MAPPING_START | rows={}", rows == null ? 0 : rows.size());

        FmOutletDetailsDto outlet = new FmOutletDetailsDto();

        // =========================================================
        // Validate merchant outlet data
        // =========================================================

        if (rows == null || rows.isEmpty()) {

            log.warn("No merchant outlet menu data found");

            return outlet;
        }

        // =========================================================
        // STEP 1: Map outlet-level details
        // From: jippy_fm.outlets
        // =========================================================

        FmMerchantOutletMenuProjection firstRow = rows.get(0);

        outlet.setOutletId(firstRow.getOutletId());
        outlet.setOutletName(firstRow.getOutletName());
        outlet.setOutletEmail(firstRow.getOutletEmail());
        outlet.setOutletPhone(firstRow.getOutletPhone());
        outlet.setAlternateOutletPhone(firstRow.getAlternateOutletPhone());

        outlet.setLatitude(firstRow.getLatitude());
        outlet.setLongitude(firstRow.getLongitude());
        outlet.setIsAvailable(firstRow.getOutletAvailable());

        // =========================================================
        // STEP 2: Map outlet bank details
        // From: jippy_fm.user_bank_details
        // =========================================================

        outlet.setAccountNumber(firstRow.getAccountNumber());
        outlet.setIfscCode(firstRow.getIfscCode());
        outlet.setBankName(firstRow.getBankName());
        outlet.setAccountHolderName(firstRow.getAccountHolderName());

        // =========================================================
        // STEP 3: Map outlet address
        // From: jippy_fm.address
        // =========================================================

        outlet.setBuildingNumber(firstRow.getBuildingNumber());
        outlet.setRoad(firstRow.getRoad());
        outlet.setLandmark(firstRow.getLandmark());

        outlet.setCityId(firstRow.getCityId());
        outlet.setCityName(firstRow.getCityName());

        outlet.setStateId(firstRow.getStateId());
        outlet.setStateName(firstRow.getStateName());

        outlet.setAreaId(firstRow.getAreaId());
        outlet.setAreaName(firstRow.getAreaName());

        // =========================================================
        // STEP 4: Maps used to avoid duplicate data
        // =========================================================

        Map<Integer, FmCategoryDto> categoryMap = new LinkedHashMap<>();

        Map<String, FmOutletTimingDto> outletTimingMap = new LinkedHashMap<>();

        // Cuisine can appear multiple times because of
        // Category × Product × Variant × Timing joins.
        Map<Integer, FmCuisineTypeResponseDTO> cuisineTypeMap = new LinkedHashMap<>();

        // =========================================================
        // STEP 5: Process every query row
        // =========================================================

        for (FmMerchantOutletMenuProjection row : rows) {

            // =====================================================
            // Cuisine Type
            // From: jippy_fm.cuisine_types
            //
            // outlets.cuisine_type contains INTEGER[]
            // Example: [1,2]
            //
            // cuisine_types:
            // 1 -> INDIAN
            // 2 -> CHINESE
            // =====================================================

            if (row.getCuisineTypeId() != null) {

                Integer cuisineTypeId = row.getCuisineTypeId();

                // Prevent duplicate cuisine types
                if (!cuisineTypeMap.containsKey(cuisineTypeId)) {

                    FmCuisineTypeResponseDTO cuisineDto = new FmCuisineTypeResponseDTO();

                    cuisineDto.setCuisineTypeId(cuisineTypeId);

                    cuisineDto.setCuisineTypeName(row.getCuisineTypeName());

                    cuisineTypeMap.put(cuisineTypeId, cuisineDto);
                }
            }

            // =====================================================
            // Outlet Timings
            // From: jippy_fm.outlet_days
            // =====================================================

            if (row.getOutletDay() != null) {

                String timingKey = row.getOutletDay() + "_" + row.getOpeningTime() + "_" + row.getClosingTime();

                if (!outletTimingMap.containsKey(timingKey)) {

                    FmOutletTimingDto timing = new FmOutletTimingDto();

                    timing.setDay(row.getOutletDay());
                    timing.setOpeningTime(row.getOpeningTime());
                    timing.setClosingTime(row.getClosingTime());
                    timing.setIsOpen(row.getIsOpen());

                    outletTimingMap.put(timingKey, timing);
                }
            }

            // =====================================================
            // Category
            // From: jippy_fm.categories
            // =====================================================

            if (row.getCategoryId() == null) {
                continue;
            }

            FmCategoryDto category = categoryMap.get(row.getCategoryId());

            if (category == null) {

                category = new FmCategoryDto();

                category.setCategoryId(row.getCategoryId());

                category.setCategoryName(row.getCategoryName());

                category.setIsAvailable(row.getCategoryAvailable());

                category.setProducts(new ArrayList<>());

                categoryMap.put(row.getCategoryId(), category);
            }

            // =====================================================
            // Product
            // From: jippy_fm.products
            // =====================================================

            if (row.getProductId() == null) {
                continue;
            }

            FmProductDto product = null;

            // Check whether product is already mapped
            // because the same product can appear in multiple
            // variant/timing rows.

            for (FmProductDto existingProduct : category.getProducts()) {

                if (existingProduct.getProductId().equals(row.getProductId())) {

                    product = existingProduct;

                    break;
                }
            }

            // Create product only once
            if (product == null) {

                product = new FmProductDto();

                product.setProductId(row.getProductId());

                product.setProductName(row.getProductName());

                product.setDescription(row.getDescription());

                product.setImageLink(row.getImageLink());

                // =================================================
                // Merchant Product Price
                // From: jippy_fm.products.merchant_price
                // =================================================

                product.setMerchantPrice(row.getMerchantPrice());

                // Merchant should not receive online price
                product.setOnlinePrice(null);

                product.setIsVeg(row.getIsVeg());

                product.setHasProductVariants(row.getHasProductVariants());

                product.setIsAvailable(row.getProductAvailable());

                product.setProductTimings(new ArrayList<>());

                product.setVariants(new ArrayList<>());

                category.getProducts().add(product);
            }

            // =====================================================
            // Product Timings
            // From: jippy_fm.product_available_timings
            // =====================================================

            if (row.getStartTime() != null) {

                boolean timingExists = false;

                for (FmProductTimingDto existingTiming : product.getProductTimings()) {

                    if (existingTiming.getDay().equals(row.getProductDay()) && existingTiming.getStartTime().equals(row.getStartTime()) && existingTiming.getEndTime().equals(row.getEndTime())) {

                        timingExists = true;

                        break;
                    }
                }

                if (!timingExists) {

                    FmProductTimingDto timing = new FmProductTimingDto();

                    timing.setDay(row.getProductDay());

                    timing.setStartTime(row.getStartTime());

                    timing.setEndTime(row.getEndTime());

                    product.getProductTimings().add(timing);
                }
            }

            // =====================================================
            // Product Variants
            // From:
            // jippy_fm.product_variant_options
            // jippy_fm.product_variant_group_values
            // jippy_fm.product_variant_groups
            // =====================================================

            if (Boolean.TRUE.equals(row.getHasProductVariants()) && row.getProductVariantId() != null) {

                boolean variantExists = false;

                for (FmProductVariantDTO existingVariant : product.getVariants()) {

                    if (existingVariant.getVariantId().equals(row.getProductVariantId())) {

                        variantExists = true;

                        break;
                    }
                }

                // Create variant only once
                if (!variantExists) {

                    FmProductVariantDTO variant = new FmProductVariantDTO();

                    variant.setVariantId(row.getProductVariantId());

                    variant.setVariantName(row.getVariantName());

                    variant.setGroupName(row.getVariantGroupName());

                    variant.setPriceType(row.getVariantPriceType());

                    // =================================================
                    // Merchant Variant Price
                    // From:
                    // jippy_fm.product_variant_options.variant_price
                    // =================================================

                    variant.setMerchantPrice(row.getVariantMerchantPrice());

                    // Merchant should not receive online price
                    variant.setOnlinePrice(null);

                    product.getVariants().add(variant);
                }
            }
        }

        // =========================================================
        // STEP 6: Set final outlet-level collections
        // =========================================================

        outlet.setCuisineTypes(new ArrayList<>(cuisineTypeMap.values())

        );

        outlet.setOutletTimings(new ArrayList<>(outletTimingMap.values()));

        outlet.setCategories(new ArrayList<>(categoryMap.values()));

        // =========================================================
        // STEP 7: Mapping completed
        // =========================================================

        log.info("MERCHANT_OUTLET_MAPPING_COMPLETED | " + "outletId={} | cuisines={} | categories={}", outlet.getOutletId(), cuisineTypeMap.size(), categoryMap.size());

        return outlet;
    }
//    ==============================================================================================
//    ==============================================================================================

//    ==============================================================================================
//    ------------update outlet details mapping to dto for merchant userType ONLY-----------------
//    ==============================================================================================

    /**
     * Updates outlet details.
     *
     * @param outlet Existing outlet entity
     * @param dto    Update request DTO
     */
    public static void updateOutletEntity(FmOutlet outlet, FmUpdateOutletRequestDTO dto) {

        outlet.setOutletName(dto.getOutletName());

        outlet.setMerchantId(dto.getMerchantId());

        outlet.setCuisineType(dto.getCuisineType());

        outlet.setOutletPhone(dto.getOutletPhone());

        outlet.setUpdatedBy(dto.getUpdatedBy());

        /*
         * Update outlet location.
         */
        if (dto.getLatitude() != null && dto.getLongitude() != null) {

            GeometryFactory geometryFactory = new GeometryFactory();

            Point point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(dto.getLongitude()), Double.parseDouble(dto.getLatitude())));
            point.setSRID(4326);
            outlet.setOutletLocation(point);
        }
    }

    /**
     * Updates outlet address details.
     *
     * @param address Existing outlet address entity
     * @param dto     Update request DTO
     */
    public static void updateOutletAddressEntity(FmOutletAddress address, FmUpdateOutletRequestDTO dto) {

        address.setBuildingNumber(dto.getBuildingNumber());
        address.setRoad(dto.getRoad());
        address.setLandmark(dto.getLandmark());

        address.setStateId(dto.getStateId());
        address.setCityId(dto.getCityId());
        address.setAreaId(dto.getAreaId());
    }

    /**
     * Updates outlet bank details.
     *
     * @param bankDetails Existing bank details entity
     * @param dto         Update request DTO
     */
    public static void updateOutletBankEntity(FmMerchantBankDetails bankDetails, FmUpdateOutletRequestDTO dto) {

        bankDetails.setAccountNumber(dto.getAccountNumber());

        bankDetails.setIfscCode(dto.getIfscCode());

        bankDetails.setBankName(dto.getBankName());

        bankDetails.setAccountHolderName(dto.getAccountHolderName());
    }
//    ------------------------------------------------------------------------------------------------

    /**
     * Maps projection results to a list of DTOs for Merchant-view outlet listings.
     */
    public static List<FmOutletByMerchantDto> mapToOutletByMerchantDto(List<FmOutletByMerchantProjection> rows) {
        log.debug("Mapping {} All outlet rows", rows.size());
        List<FmOutletByMerchantDto> dtoList = new ArrayList<>();

        for (FmOutletByMerchantProjection row : rows) {
            if (row != null) {
                FmOutletByMerchantDto dto = new FmOutletByMerchantDto();
                dto.setOutletId(row.getOutletId());
                dto.setOutletName(row.getOutletName());
                dto.setOutletPhone(row.getOutletPhone());
                dto.setIsApproved(row.getIsApproved());
                dto.setStateName(row.getStateName());
                dto.setCityName(row.getCityName());
                dto.setAreaName(row.getAreaName());
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    public static FmOutletDto mapRowToDto(Object[] row) {
        FmOutletDto dto = new FmOutletDto();
        dto.setOutletId(toInt(row[0]));
        dto.setOutletName(row[1] != null ? row[1].toString() : null);
        dto.setMerchantId(toInt(row[2]));
        dto.setCuisineType(row[3] != null ? (Integer[]) row[3] : null);
        dto.setOutletPhone(row[4] != null ? row[4].toString() : null);
        dto.setRadius(toBigDecimal(row[5]));
        dto.setReview(toDouble(row[6]));
        dto.setSubscriptionStatus(row[7] != null ? row[7].toString() : null);
        dto.setPromotionStatus(row[8] != null ? row[8].toString() : null);
        dto.setIsActive(row[9] != null && "Y".equalsIgnoreCase(row[9].toString()));
        dto.setIsApproved(row[10] != null && (Boolean) row[10]);
        // distance_km only present in nearby query (index 16)
        if (row.length > 16 && row[16] != null) {
            dto.setDistanceKm(toDouble(row[16]));
        }
        return dto;
    }

    private static Integer toInt(Object o) {

        return o == null ? null : ((Number) o).intValue();
    }

    private static Double toDouble(Object o) {

        return o == null ? null : Double.parseDouble(o.toString());
    }

    /**
     * Converts outlet bank details from request DTO to User Bank Details entity.
     * <p>
     * Why:
     * Keeps all entity mapping inside the mapper instead of the service.
     */
    public static FmMerchantBankDetails toOutletBankEntity(FmOutletRequestDTO dto, Integer outletId) {

        FmMerchantBankDetails bankDetails = new FmMerchantBankDetails();

        bankDetails.setRecipientId(outletId);

        bankDetails.setAccountNumber(dto.getAccountNumber());

        bankDetails.setIfscCode(dto.getIfscCode());

        bankDetails.setBankName(dto.getBankName());

        bankDetails.setAccountHolderName(dto.getAccountHolderName());

        // Store as OUTLET in user_bank_details table
        bankDetails.setUserType(FmAppConstants.TYPE_OUTLET);

        return bankDetails;
    }

    /**
     * Converts the created outlet entity and request DTO into
     * the response DTO returned after successful outlet creation.
     */

    public static FmOutletCreateResponseDTO toCreateResponseDto(FmOutletRequestDTO request, FmOutlet outlet) {

        FmOutletCreateResponseDTO response = new FmOutletCreateResponseDTO();

        // ---------------- Outlet Details ----------------

        response.setOutletId(outlet.getOutletId());
        response.setOutletName(outlet.getOutletName());
        response.setMerchantId(outlet.getMerchantId());
        response.setCuisineType(outlet.getCuisineType());
        response.setOutletPhone(outlet.getOutletPhone());
        response.setOutletEmail(request.getOutletEmail());
        response.setAlternateOutletPhone(request.getAlternateOutletPhone());
        response.setFssaiNumber(request.getFssaiNumber());
        response.setGstNumber(request.getGstNumber());
        response.setUsername(request.getUsername());
        response.setUpdatedBy(request.getUpdatedBy());

        // Never expose actual password
        response.setPassword("********");

        response.setIsActive(outlet.getIsActive());

        // ---------------- Bank Details ----------------

        response.setAccountNumber(request.getAccountNumber());
        response.setIfscCode(request.getIfscCode());
        response.setBankName(request.getBankName());
        response.setAccountHolderName(request.getAccountHolderName());

        // ---------------- Address ----------------

        response.setBuildingNumber(request.getBuildingNumber());
        response.setRoad(request.getRoad());
        response.setLandmark(request.getLandmark());
        response.setCityId(request.getCityId());
        response.setStateId(request.getStateId());
        response.setAreaId(request.getAreaId());
        response.setLatitude(request.getLatitude());
        response.setLongitude(request.getLongitude());

        response.setOutletPicUrl(outlet.getOutletPicUrl());


        // ---------------- Operating Days ----------------

        response.setOperatingDays(request.getOperatingDays());

        // ---------------- Tracking ----------------

        response.setUpdatedBy(request.getUpdatedBy());

        return response;
    }

//    ------------------------------------------------------------------------------------------

    /**
     * Converts the updated outlet entity and request DTO into
     * the response returned after successful outlet update.
     * <p>
     * Note:
     * Username and Password are not part of the update API.
     */
    public static FmUpdateOutletRequestDTO toUpdateResponseDto(FmUpdateOutletRequestDTO request, FmOutlet outlet) {

        FmUpdateOutletRequestDTO response = new FmUpdateOutletRequestDTO();

        // ---------------- Outlet Details ----------------

        response.setOutletName(outlet.getOutletName());
        response.setMerchantId(outlet.getMerchantId());
        response.setOutletEmail(outlet.getOutletEmail());
        response.setCuisineType(outlet.getCuisineType());
        response.setOutletPhone(outlet.getOutletPhone());
        response.setAlternateOutletPhone(outlet.getAlternateOutletPhone());

        // ---------------- Tracking ----------------
        response.setUpdatedBy(request.getUpdatedBy());

        // ---------------- Bank Details ----------------

        response.setAccountNumber(request.getAccountNumber());
        response.setIfscCode(request.getIfscCode());
        response.setBankName(request.getBankName());
        response.setAccountHolderName(request.getAccountHolderName());

        // ---------------- Address Details ----------------

        response.setBuildingNumber(request.getBuildingNumber());
        response.setRoad(request.getRoad());
        response.setLandmark(request.getLandmark());
        response.setStateId(request.getStateId());
        response.setCityId(request.getCityId());
        response.setAreaId(request.getAreaId());

        // ---------------- Outlet Location ----------------

        response.setLatitude(request.getLatitude());
        response.setLongitude(request.getLongitude());

        // ---------------- Operating Days ----------------

        response.setOperatingDays(request.getOperatingDays());


        return response;
    }

    public static FmOutletDay toOutletDayEntity(FmOutletDayDTO dto, Integer outletId) {

        FmOutletDay day = new FmOutletDay();

        day.setOutletId(outletId);
        day.setDayOfWeekId(dto.getDayOfWeekId());
        day.setIsOpen(dto.getIsOpen());
        day.setOpeningTime(dto.getOpeningTime());
        day.setClosingTime(dto.getClosingTime());

        return day;
    }

    private static BigDecimal toBigDecimal(Object o) {

        if (o == null) {
            return null;
        }

        if (o instanceof BigDecimal) {
            return (BigDecimal) o;
        }

        return new BigDecimal(o.toString());
    }

    public static FmActiveDiscountsDto mapToActiveDiscounts(FmActiveDiscountsResponseDto outletDiscounts) {

        FmActiveDiscountsDto activeOffer = new FmActiveDiscountsDto();

        activeOffer.setDiscountAmount(outletDiscounts.getDiscountAmount());
        activeOffer.setPriceType(outletDiscounts.getPriceType());
        activeOffer.setCouponCode(outletDiscounts.getCouponCode());
        activeOffer.setMinOrderValue(outletDiscounts.getMinOrderValue());
        activeOffer.setEndDateTime(outletDiscounts.getEndDateTime());
        activeOffer.setSourceId(outletDiscounts.getSourceId());
        activeOffer.setSourceType(outletDiscounts.getSourceType());
        activeOffer.setStartDateTime(outletDiscounts.getStartDateTime());
        activeOffer.setPromotionScheduleId(outletDiscounts.getPromotionScheduleId());
        activeOffer.setUsageLimitPerUser(outletDiscounts.getUsageLimitPerUser());

        if (outletDiscounts.getSourceType().equals("PRICE_DROP")) {

            log.info("Outlet has price drop ");
            Duration duration = Duration.between(LocalDateTime.now(), outletDiscounts.getEndDateTime());

            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();
            String remainingTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            activeOffer.setRemainingTime(remainingTimeStr);

            log.info("===================={} ", remainingTimeStr);
        }

        return activeOffer;
    }
}