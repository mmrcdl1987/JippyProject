//package com.jippy.foodandmart.mapper;
//
//import com.jippy.foodandmart.dto.FmOutletCreatedDTO;
//import com.jippy.foodandmart.dto.FmOutletRequestDTO;
//import com.jippy.foodandmart.entity.FmOutlet;
//import com.jippy.foodandmart.entity.FmOutletAddress;
//
///**
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
import com.jippy.division.dto.FmNearbyOutletDto;
import com.jippy.foodandmart.dto.FmOutletDto;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmOutletAddress;
import com.jippy.foodandmart.projections.FmOutletByMerchantProjection;
import com.jippy.foodandmart.projections.FmOutletMenuProjection;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        outlet.setCuisineType(dto.getCuisineType().trim());
        outlet.setOutletPhone(dto.getOutletPhone().trim());
        outlet.setIsActive("Y");
        return outlet;
    }

    public static  FmAddressRequestDto toAddressRequestDto(FmOutletAddress fmOutletAddress){
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

//    for feign client to map the address details from Outletdto to AddressRequestdto to send to address microservice
    public static  FmAddressRequestDto convertToAddressReqDto(FmOutletRequestDTO dto, Integer outletId, Integer stateId,Integer areaId){
 FmAddressRequestDto fmAddressRequestDto = new FmAddressRequestDto();
 fmAddressRequestDto.setAddressType(FmAppConstants.TYPE_OUTLET);
 fmAddressRequestDto.setJippyAddressId(outletId);
 fmAddressRequestDto.setBuildingNumber(dto.getBuildingNumber());
 fmAddressRequestDto.setRoad(dto.getRoad());
 fmAddressRequestDto.setLandmark(dto.getLandmark());
 fmAddressRequestDto.setCityId(dto.getCityId()!=null?dto.getCityId():0);
    fmAddressRequestDto.setStateId(stateId);
    fmAddressRequestDto.setAreaId(areaId);
 return fmAddressRequestDto;


    }

    /**
     * Builds an {@link FmOutletAddress} from the DTO plus resolved integer FKs.
     */


    public static FmOutletAddress toAddressEntity(FmAddressRequestDto dto){

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
    public static FmOutletCreatedDTO toCreatedDTO(FmOutlet outlet, String loginId, String password) {
        FmOutletCreatedDTO dto = new FmOutletCreatedDTO();
        dto.setOutletId(outlet.getOutletId());
        dto.setOutletName(outlet.getOutletName());
        dto.setMerchantId(outlet.getMerchantId());
        dto.setCuisineType(outlet.getCuisineType());
        dto.setOutletPhone(outlet.getOutletPhone());
        dto.setIsActive(outlet.getIsActive());
        dto.setOutletLoginId(loginId);
        dto.setOutletPassword(password);
        return dto;
    }

    // ── Projection → DTO (Complex Menu Mapping) ───────────────────────────────

    /**
     * Maps flat database result sets (Projections) into a nested Outlet Hierarchy.
     */
    public static FmOutletDetailsDto mapToOutletDto(List<FmOutletMenuProjection> rows, String userType) {
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
                        c.setProducts(new ArrayList<>());
                        return c;
                    });

                    // Map Products
                    if (row.getProductId() != null) {
                        FmProductDto product = category.getProducts().stream()
                                .filter(p -> p.getProductId().equals(row.getProductId()))
                                .findFirst()
                                .orElse(null);

                        if (product == null) {
                            product = new FmProductDto();
                            product.setProductId(row.getProductId());
                            product.setProductName(row.getProductName());
                            product.setDescription(row.getDescription());

                            // Pricing logic
                            if ("CUSTOMER".equalsIgnoreCase(userType) && row.getOnlinePrice() != null) {
                                product.setPrice(row.getOnlinePrice());
                            } else {
                                product.setPrice(row.getMerchantPrice());
                            }

                            product.setIsVeg(row.getIsVeg());
                            product.setHasProductVariants(row.getHasProductVariants());
                            product.setProductTimings(new ArrayList<>());
                            category.getProducts().add(product);
                        }

                        // Map Product Timings
                        if (row.getStartTime() != null) {
                            FmProductTimingDto pt = new FmProductTimingDto();
                            pt.setDay(row.getProductDay());
                            pt.setStartTime(row.getStartTime());
                            pt.setEndTime(row.getEndTime());
                            product.getProductTimings().add(pt);
                        }
                    }
                }
            }
        }

        outlet.setOutletTimings(new ArrayList<>(outletTimingMap.values()));
        outlet.setCategories(new ArrayList<>(categoryMap.values()));
        return outlet;
    }

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
        dto.setCuisineType(row[3] != null ? row[3].toString() : null);
        dto.setOutletPhone(row[4] != null ? row[4].toString() : null);
        dto.setRadius(toDouble(row[5]));
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
}