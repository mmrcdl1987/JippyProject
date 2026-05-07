package com.jippy.foodandmart.mapper;



import com.jippy.division.dto.FmNearbyOutletDto;

/**
 * Maps a raw query row from OutletRepository.findCustomerNearbyOutlets
 * to NearbyOutletDto.
 *
 * Column index reference:
 *  [0]  outlet_id
 *  [1]  outlet_name
 *  [2]  merchant_id        (not exposed in customer DTO)
 *  [3]  cuisine_type
 *  [4]  outlet_phone
 *  [5]  radius
 *  [6]  review
 *  [7]  subscription_status
 *  [8]  promotion_status
 *  [9]  is_active
 *  [10] is_approved
 *  [11] employee_id        (not exposed)
 *  [12] created_at         (not exposed)
 *  [13] created_by         (not exposed)
 *  [14] updated_at         (not exposed)
 *  [15] updated_by         (not exposed)
 *  [16] distance_km
 *  [17] outlet_lat         ← for Google Maps (not in DTO)
 *  [18] outlet_lng         ← for Google Maps (not in DTO)
 *
 * openingTime, closingTime, openNow, roadDistance, deliveryTime
 * are populated by OutletServiceImpl after the map() call.
 */
public class FmNearbyOutletMapper {

    public static FmNearbyOutletDto map(Object[] row) {
        FmNearbyOutletDto dto = new FmNearbyOutletDto();
        dto.setOutletId(toInt(row[0]));
        dto.setOutletName(str(row[1]));
        dto.setCuisineType(str(row[3]));
        dto.setOutletPhone(str(row[4]));
        dto.setRadius(toDouble(row[5]));
        dto.setReview(toDouble(row[6]));
        dto.setSubscriptionStatus(str(row[7]));
        dto.setPromotionStatus(str(row[8]));
        dto.setDistanceKm(toDouble(row[16]));
        // [17] and [18] extracted separately via helpers below
        return dto;
    }

    /** Outlet latitude from column [17] — used by OutletServiceImpl for Google Maps. */
    public static Double extractOutletLat(Object[] row) {
        return row.length > 17 ? toDouble(row[17]) : null;
    }

    /** Outlet longitude from column [18] — used by OutletServiceImpl for Google Maps. */
    public static Double extractOutletLng(Object[] row) {
        return row.length > 18 ? toDouble(row[18]) : null;
    }

    // ── type helpers ─────────────────────────────────────────────────────────

    private static Integer toInt(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }

    private static Double toDouble(Object o) {
        return o == null ? null : Double.parseDouble(o.toString());
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }
}

