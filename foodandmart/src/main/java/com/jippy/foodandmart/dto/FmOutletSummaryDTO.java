
        package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmOutletAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lightweight summary DTO for outlet list views and dropdowns.
 *
 * <p>Contains both master IDs and display names so API consumers can
 * render human-readable values while still retaining IDs for navigation,
 * edit, and update operations.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmOutletSummaryDTO {

    private Integer outletId;

    // ── Merchant ──────────────────────────────────────────────────────────────

    private Integer merchantId;

    private String merchantName;

    // ── Outlet ────────────────────────────────────────────────────────────────

    private String outletName;

    // ── Cuisine ───────────────────────────────────────────────────────────────

    private Integer[] cuisineType;

    private String[] cuisineNames;

    private String outletPhone;

    private String isActive;

    /**
     * Number of menu items in this outlet.
     */
    private long menuItemCount;

    // ── Address ───────────────────────────────────────────────────────────────

    private Integer stateId;

    private String stateName;

    private Integer areaId;

    private String areaName;

    private String road;

    private String landmark;

    private String buildingNumber;

    /**
     * Creates a summary DTO from an outlet entity with a menu item count.
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
     * Creates a summary DTO from an outlet entity, item count,
     * and optional address.
     */
    public static FmOutletSummaryDTO from(
            FmOutlet o,
            long itemCount,
            FmOutletAddress addr
    ) {

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
