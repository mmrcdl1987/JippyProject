package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmOutletAddress;
import lombok.*;

/**
 * Lightweight summary DTO for outlet list views and dropdowns.
 *
 * <p>Why a summary DTO instead of returning the full {@link FmOutlet} entity:
 * the list view only needs a subset of fields. Returning the full entity
 * risks triggering lazy-loaded associations (merchant, address, days) and
 * exposes internal JPA details to API consumers.</p>
 *
 * <p>Address fields are optional — an outlet may exist before its physical
 * address is registered (e.g. during the initial onboarding step).</p>
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletSummaryDTO {

    private Integer outletId;
    private Integer merchantId;
    private String  outletName;
    private String  cuisineType;
    private String  outletPhone;
    private String  isActive;

    /** Number of menu items in this outlet — used by Copy Menu UI card badges. */
    private long    menuItemCount;

    // ── Address fields (optional) ─────────────────────────────────────────────

    /** FK to jippy_fm.states — integer state identifier. */
    private Integer stateId;

    /** FK to jippy_fm.area — integer area identifier resolved from area name during upload. */
    private Integer areaId;
    private String  road;
    private String  landmark;
    private String  buildingNumber;

    /**
     * Creates a summary DTO from an {@link FmOutlet} entity with a menu item count.
     *
     * <p>Why this overload: used by the menu service which has the item count
     * from a {@code COUNT} query but does not load address data.</p>
     *
     * @param o         the outlet entity
     * @param itemCount the number of menu items for this outlet
     * @return a summary DTO without address fields populated
     */
    public static FmOutletSummaryDTO from(FmOutlet o, long itemCount) {
        FmOutletSummaryDTO dto = new FmOutletSummaryDTO();
        dto.setOutletId(o.getOutletId());
        dto.setMerchantId(o.getMerchantId());
        dto.setOutletName(o.getOutletName());
        dto.setCuisineType(o.getCuisineType());
        dto.setOutletPhone(o.getOutletPhone());
        dto.setIsActive(o.getIsActive());
        dto.setMenuItemCount(itemCount);
        return dto;
    }

    /**
     * Creates a summary DTO from an outlet entity, item count, and optional address.
     *
     * <p>Why accept a nullable {@link FmOutletAddress}: the outlet summary list
     * page shows address fields when available. Passing null for newly created
     * outlets (before an address is registered) is safe — those fields stay null
     * in the DTO and are omitted or shown as blank in the UI.</p>
     *
     * @param o         the outlet entity
     * @param itemCount the number of menu items
     * @param addr      the outlet's address entity, or null if not yet saved
     * @return a summary DTO with address fields populated if addr is non-null
     */
    public static FmOutletSummaryDTO from(FmOutlet o, long itemCount, FmOutletAddress addr) {
        // Delegate to the simpler overload, then add address fields
        FmOutletSummaryDTO dto = from(o, itemCount);
        if (addr != null) {
            dto.setStateId(addr.getStateId());
            dto.setAreaId(addr.getAreaId());
            dto.setRoad(addr.getRoad());
            dto.setLandmark(addr.getLandmark());
            dto.setBuildingNumber(addr.getBuildingNumber());
        }
        return dto;
    }
}
