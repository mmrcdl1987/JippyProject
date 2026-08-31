    package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmSpecializedOutlet;
import com.jippy.foodandmart.projections.FmNearbyOutletProjection;
import com.jippy.foodandmart.projections.FmPublicNearbyOutletProjection;
import com.jippy.foodandmart.projections.FmOutletProjection;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.List;

    @Repository
    public interface FmSpecializedOutletRepository
            extends JpaRepository<FmSpecializedOutlet, Integer> {

        @Query(value = """
SELECT DISTINCT
    o.outlet_id AS outletId,
    o.outlet_name AS outletName
FROM jippy_fm.specialized_outlets so
JOIN jippy_fm.outlets o
    ON so.outlet_id = o.outlet_id
WHERE so.area_id = :areaId
AND o.is_active = 'Y'
""", nativeQuery = true)
        List<FmOutletProjection>
        fetchSpecializedOutletsByAreaId(
                @Param("areaId") Integer areaId
        );
//        @Query(value = """
//SELECT
//    o.outlet_id AS outletId,
//    o.outlet_name AS outletName,
//
//    ST_Distance(
//        o.outlet_location,
//        ST_SetSRID(
//            ST_MakePoint(
//                :longitude,
//                :latitude
//            ),
//            4326
//        )::geography
//    ) / 1000 AS distanceInKm
//
//FROM jippy_fm.specialized_outlets so
//
//JOIN jippy_fm.outlets o
//ON so.outlet_id = o.outlet_id
//
//WHERE o.is_active = 'Y'
//AND o.is_approved = true
//AND o.outlet_location IS NOT NULL
//
//AND ST_Distance(
//        o.outlet_location,
//        ST_SetSRID(
//            ST_MakePoint(
//                :longitude,
//                :latitude
//            ),
//            4326
//        )::geography
//    ) <= :radius
//
//GROUP BY
//    o.outlet_id,
//    o.outlet_name,
//    o.outlet_location
//
//ORDER BY distanceInKm=
//""", nativeQuery = true)
//        List<FmNearbyOutletProjection>
//        fetchNearbySpecializedOutlets(
//                @Param("latitude") Double latitude,
//                @Param("longitude") Double longitude,
//                @Param("radius") Double radius);

        @Query(value = """
SELECT
    o.outlet_id      AS outletId,
    o.outlet_name    AS outletName,
    o.merchant_id    AS merchantId,
    o.cuisine_type   AS cuisineType,
    o.outlet_phone   AS outletPhone,
    o.radius         AS radius,

    ST_Distance(
        o.outlet_location,
        ST_SetSRID(
            ST_MakePoint(
                :longitude,
                :latitude
            ),
            4326
        )::geography
    ) / 1000 AS distanceInKm

FROM jippy_fm.specialized_outlets so

JOIN jippy_fm.outlets o
    ON so.outlet_id = o.outlet_id

WHERE o.is_active = 'Y'
  AND o.is_approved = true
  AND o.outlet_location IS NOT NULL

  AND ST_Distance(
        o.outlet_location,
        ST_SetSRID(
            ST_MakePoint(
                :longitude,
                :latitude
            ),
            4326
        )::geography
    ) <= :radius

GROUP BY
    o.outlet_id,
    o.outlet_name,
    o.merchant_id,
    o.cuisine_type,
    o.outlet_phone,
    o.radius,
    o.outlet_location

ORDER BY distanceInKm ASC
""", nativeQuery = true)
        List<FmNearbyOutletProjection> fetchNearbySpecializedOutlets(
                @Param("latitude") Double latitude,
                @Param("longitude") Double longitude,
                @Param("radius") Double radius);

        @Query(value = """
SELECT
    o.outlet_id      AS outletId,
    o.outlet_name    AS outletName,
    o.merchant_id    AS merchantId,
    o.outlet_phone   AS outletPhone,
    o.radius         AS radius,
    o.subscription_status AS subscriptionStatus,
    o.promotion_status    AS promotionStatus,
    o.review         AS review,
    CASE WHEN o.is_active = 'Y' THEN true ELSE false END AS isActive,
    o.is_approved    AS isApproved,

    ST_Distance(
        o.outlet_location,
        ST_SetSRID(
            ST_MakePoint(
                :longitude,
                :latitude
            ),
            4326
        )::geography
    ) / 1000 AS distanceInKm

FROM jippy_fm.specialized_outlets so

JOIN jippy_fm.outlets o
    ON so.outlet_id = o.outlet_id

WHERE o.is_active = 'Y'
  AND o.is_approved = true
  AND o.outlet_location IS NOT NULL

  AND ST_Distance(
        o.outlet_location,
        ST_SetSRID(
            ST_MakePoint(
                :longitude,
                :latitude
            ),
            4326
        )::geography
    ) <= :radius

GROUP BY
    o.outlet_id,
    o.outlet_name,
    o.merchant_id,
    o.outlet_phone,
    o.radius,
    o.subscription_status,
    o.promotion_status,
    o.review,
    o.is_active,
    o.is_approved,
    o.outlet_location

ORDER BY distanceInKm ASC
""", nativeQuery = true)
        List<FmPublicNearbyOutletProjection> fetchPublicNearbySpecializedOutlets(
                @Param("latitude") Double latitude,
                @Param("longitude") Double longitude,
                @Param("radius") Double radius);
    }
