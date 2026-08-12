package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.dto.OutletLocationProjection;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.projections.FmOutletByMerchantProjection;
import com.jippy.foodandmart.projections.FmOutletMenuProjection;
import com.jippy.foodandmart.projections.FmOutletSettlementProjection;
import com.jippy.foodandmart.projections.FmPendingOutletApprovalProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmOutletRepository extends JpaRepository<FmOutlet, Integer> {
    Optional<FmOutlet> findByOutletPhone(String phone);

    boolean existsByOutletPhone(String phone);

    boolean existsByMerchantIdAndOutletName(Integer merchantId, String outletName);

    List<FmOutlet> findByMerchantId(Integer merchantId);


    // for finding the email in te outlet table
    Optional<FmOutlet> findByOutletEmailIgnoreCase(String outletEmail);

    @Query(value = """
               SELECT
                           -- Outlet basic details (from outlets table)
                           o.outlet_id,          -- jippy_fm.outlets
                           o.outlet_name,        -- jippy_fm.outlets
                           o.outlet_email,       -- jippy_fm.outlets
                           o.outlet_phone,       -- jippy_fm.outlets
                           o.alternate_outlet_phone,   -- jippy_fm.outlets
                           o.cuisine_type,       -- jippy_fm.outlets
            
                           --Location details 
                           ST_Y(o.outlet_location::geometry) AS latitude,
                           ST_X(o.outlet_location::geometry) AS longitude,
                           o.is_toggle AS outlet_available,     -- jippy_fm.outlets
            
                           -- Outlet bank details (from user_bank_details table)
                           ubd.account_number,
                           ubd.ifsc_code,
                           ubd.bank_name,
                           ubd.account_holder_name,
            
                           -- Outlet address details (from address table)
                           a.building_number,
                           a.road,
                           a.landmark,
                           a.city_id,
                           ct.city_name,
                           a.state_id,
                           st.state_name,
                           a.area_id,
                           ar.area_name,
            
            
                           --- online pricing details (from product_online_pricing table)
                           --product_id from product_online_pricing table 
                           pop.product_id AS pop_id,          -- jippy_fm.product_online_pricing
                           pop.online_price,        -- jippy_fm.product_online_pricing
            
                           -- Category details (from categories table)
                           c.category_id,        -- jippy_fm.categories
                           c.category_name,      -- jippy_fm.categories
                           oc.is_toggle AS category_available,   -- jippy_fm.categories
            
            
                           -- Product details (from products table)
                           p.product_id,         -- jippy_fm.products
                           p.product_name,       -- jippy_fm.products
                           p.description,        -- jippy_fm.products
                           p.merchant_price,     -- jippy_fm.products
                           p.is_veg,             -- jippy_fm.products
                           p.has_product_variants, -- jippy_fm.products
                           p.is_toggle AS product_available,       -- jippy_fm.products
            
                           -- Product Variant Details (from product_variants table)
                           pv.product_variant_options_id,
                          --  pv.variant_name,
                           pv.variant_price AS variant_merchant_price,
            
            
                           -- Outlet day-wise availability (from outlet_days table)
                           od.is_open,           -- jippy_fm.outlet_days
                           od.opening_time,      -- jippy_fm.outlet_days
                           od.closing_time,      -- jippy_fm.outlet_days
            
                           -- Outlet day name (from days_of_week table via outlet_days)
                           d1.day_name AS outlet_day,   -- jippy_fm.days_of_week
            
                           -- Product available timings (from product_available_timings table)
                           pat.start_time,       -- jippy_fm.product_available_timings
                           pat.end_time,         -- jippy_fm.product_available_timings
            
                           -- Product day name (from days_of_week table via product_available_timings)
                           d2.day_name AS product_day   -- jippy_fm.days_of_week
            
            
                       -- Start from outlet (main table: jippy_fm.outlets)
                       FROM jippy_fm.outlets o
            
                       -- Fetch outlet bank details
                       LEFT JOIN jippy_fm.user_bank_details ubd
                              ON ubd.recipient_id = o.outlet_id
                             AND ubd.user_type = 'OUTLET'
            
                       -- Fetch outlet address
                       LEFT JOIN jippy_fm.address a
                              ON a.jippy_address_id = o.outlet_id
                             AND a.address_type = 'OUTLET'
            
                       -- Fetch state details
                       LEFT JOIN jippy_fm.state st
                              ON st.state_id = a.state_id
            
                       -- Fetch city details
                       LEFT JOIN jippy_fm.city ct
                              ON ct.city_id = a.city_id
            
                       -- Fetch area details
                       LEFT JOIN jippy_fm.area ar
                              ON ar.area_id = a.area_id
            
                       -- Join outlet_categories (maps outlet to categories)
                       JOIN jippy_fm.outlet_categories oc
                           ON o.outlet_id = oc.outlet_id
            
            
                       -- Join categories (get category details)
                       JOIN jippy_fm.categories c
                           ON oc.category_id = c.category_id
            
            
                         -- Join products (get products under each outlet_category)
                       JOIN jippy_fm.products p
                           ON oc.outlet_category_id = p.outlet_category_id
                       --JOIN jippy_fm.product_online_pricing pop
                            --ON p.product_id = pop.product_id
            
                -- for online pricing details 
                --(get online price for each product by product_id and outlet_category_id)
                            LEFT JOIN jippy_fm.product_online_pricing pop
                               ON p.product_id = pop.product_id
                               AND p.outlet_category_id = pop.outlet_category_id
            
                  -- Fetch product variants
                  LEFT JOIN jippy_fm.product_variant_options pv
                         ON pv.product_id = p.product_id             
            
                       -- Left join outlet_days (get outlet timings per day)
                       LEFT JOIN jippy_fm.outlet_days od
                           ON o.outlet_id = od.outlet_id
            
            
                       -- Join days_of_week for outlet days (convert day_id to name)
                       LEFT JOIN jippy_fm.days_of_week d1
                           ON od.day_of_week_id = d1.day_id
            
            
                       -- Left join product_available_timings (get product timing per day)
                       -- Condition ensures product timing matches outlet day
                       LEFT JOIN jippy_fm.product_available_timings pat
                           ON p.product_id = pat.product_id
                           AND od.day_of_week_id = pat.day_of_week_id
            
            
                       -- Join days_of_week for product days (convert day_id to name)
                       LEFT JOIN jippy_fm.days_of_week d2
                           ON pat.day_of_week_id = d2.day_id
            
            
                       -- Filter by outlet_id (input parameter from API)
            
                        WHERE o.is_approved = true AND o.outlet_id = :outletId  --for Api response @query
                       --WHERE o.is_approved = true AND o.outlet_id = 1  --for postgres SQL testing used 
            
            
                       -- Order results to simplify grouping in service layer
                       ORDER BY
                           c.category_id,
                           p.product_id,
                           od.day_of_week_id,
                           pat.start_time;
            """, nativeQuery = true)
    List<FmOutletMenuProjection> getOutletMenu(@Param("outletId") Integer outletId);

    //    for getOutletsByMerchant API - to fetch-
//    -outlet's, address-state,city,area details based on merchantId
    @Query(value = """
                -- Fetch outlet details along with location information for a given merchant
            
                SELECT
                    -- Basic outlet details
                    o.outlet_id AS outletId,
                    o.outlet_name AS outletName,
                    o.outlet_phone AS outletPhone,
                    o.is_approved AS isApproved,
                    
                    -- check if the merchant is approved or not 
                    m.is_approved AS merchantApproved,
            
            
                    -- Location details (may be NULL if address not present)
                    s.state_name AS stateName,
                    c.city_name AS cityName,
                    a.area_name AS areaName
            
                --from merchants table 
              FROM jippy_fm.merchants m
              
              --from outlets table 
              LEFT JOIN jippy_fm.outlets o
                  ON m.merchant_id = o.merchant_id
            
                -- Map outlet to address (JOIN ensures all outlets are returned even if address is missing)
                 LEFT JOIN jippy_fm.address addr
                    ON o.outlet_id = addr.jippy_address_id 
                    AND addr.address_type = 'OUTLET'
            
                -- Fetch state based on address
                LEFT JOIN jippy_fm.state s
                    ON addr.state_id = s.state_id
            
                -- Fetch city based on address
                 LEFT JOIN jippy_fm.city c
                    ON addr.city_id = c.city_id
            
                -- Fetch area based on address
                 LEFT JOIN jippy_fm.area a
                    ON addr.area_id = a.area_id
            
            -- Return all outlets for merchant
               WHERE m.merchant_id = :merchantId       --for Api response @query
            
                 --WHERE o.merchant_id = :1 -- for postgres SQL testing used
            """, nativeQuery = true)
    List<FmOutletByMerchantProjection> getOutletsByMerchantId(@Param("merchantId") Integer merchantId);

    //  UNAPPROVED OUTLETS (NO CHANGE)
    @Query(value = """
            SELECT o.*
            FROM jippy_fm.outlets o
            JOIN jippy_fm.address a 
              ON a.jippy_address_id = o.outlet_id
            WHERE a.area_id = :areaId
              AND a.address_type = :addressType
              AND o.is_approved = false
              AND (:search IS NULL OR o.outlet_name ILIKE CONCAT('%', :search, '%'))
            """, nativeQuery = true)
    List<FmOutlet> findUnapprovedOutlets(@Param("areaId") Integer areaId, @Param("addressType") String addressType, @Param("search") String search);

    // APPROVED OUTLETS (FIXED - remove duplicates properly)
    @Query(value = """
            SELECT o.*
            FROM jippy_fm.outlets o
            JOIN jippy_fm.address a 
              ON a.jippy_address_id = o.outlet_id
            WHERE a.area_id = :areaId
              AND a.address_type = :addressType
              AND EXISTS (
                  SELECT 1
                  FROM jippy_fm.product_online_pricing pop
                  JOIN jippy_fm.outlet_categories oc 
                    ON pop.outlet_category_id = oc.outlet_category_id
                  WHERE oc.outlet_id = o.outlet_id
              )
              AND (:search IS NULL OR o.outlet_name ILIKE CONCAT('%', :search, '%'))
            """, nativeQuery = true)
    List<FmOutlet> findApprovedOutlets(@Param("areaId") Integer areaId, @Param("addressType") String addressType, @Param("search") String search);


    //  APPROVE OUTLETS (NO CHANGE)
    @Modifying
    @Query(value = """
            UPDATE jippy_fm.outlets
            SET is_approved = true,
                updated_at = CURRENT_TIMESTAMP
            WHERE outlet_id IN (:ids)
            """, nativeQuery = true)
    int approveOutlets(@Param("ids") List<Integer> ids);

    @Query(value = """
            
            SELECT
                o.outlet_id,
                o.outlet_name,
                o.merchant_id,
                o.cuisine_type,
                o.outlet_phone,
                o.radius,
                o.review,
                o.subscription_status,
                o.promotion_status,
                o.is_active,
                o.is_approved,
                o.employee_id,
            
                od.opening_time,
                od.closing_time,
                od.outlet_day_id,
                od.day_of_week_id,
            
                ROUND(
                    CAST(
                        ST_Distance(
                            o.outlet_location::geography,
                            ST_SetSRID(
                                ST_MakePoint(:customerLng, :customerLat),
                                4326
                            )::geography
                        ) / 1000.0 AS numeric
                    ),
                    2
                ) AS distance_km,
            
                /*
                 * GOOGLE MAPS
                 */
            
                ST_Y(o.outlet_location::geometry) AS latitude,
            
                ST_X(o.outlet_location::geometry) AS longitude
            
            FROM jippy_fm.outlets o
            
            LEFT JOIN jippy_fm.outlet_subscription_plans osp
                   ON o.outlet_id = osp.outlet_id
                  AND CURRENT_DATE BETWEEN
                      osp.subscription_from_date
                      AND osp.subscription_to_date
            
            LEFT JOIN jippy_fm.subscription_plans sp
                   ON osp.subscription_plan_id = sp.subscription_plan_id
            
            JOIN jippy_fm.outlet_days od
                   ON od.outlet_id = o.outlet_id
            
            JOIN jippy_fm.days_of_week dow
                   ON dow.day_id = od.day_of_week_id
            
            LEFT JOIN jippy_fm.outlet_categories oc
                   ON oc.outlet_id = o.outlet_id
            
            LEFT JOIN jippy_fm.categories c
                   ON c.category_id = oc.category_id
            
            WHERE o.is_active = 'Y'
              AND o.outlet_location IS NOT NULL
              AND o.is_approved = true
              AND (:categoryId IS NULL OR c.category_id = :categoryId)
            
              AND ST_DWithin(
                    o.outlet_location::geography,
                    ST_SetSRID(
                        ST_MakePoint(:customerLng, :customerLat),
                        4326
                    )::geography,
            
                    COALESCE(sp.radius_in_kms * 1000, 3000)
                  )
            
              AND od.day_of_week_id =
                  EXTRACT(
                      ISODOW FROM
                      (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Kolkata')
                  )
            
              AND (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Kolkata')::time
                  BETWEEN od.opening_time
                  AND od.closing_time
            
            ORDER BY distance_km ASC
            
            """, nativeQuery = true)
    List<Object[]> findCustomerNearbyOutlets(@Param("customerLat") double customerLat,
                                             @Param("customerLng") double customerLng, @Param("categoryId") Integer categoryId);


    @Query(value = """
            SELECT
                outlet_id AS outletId,
                ST_Y(outlet_location::geometry) AS latitude,
                ST_X(outlet_location::geometry) AS longitude
            FROM jippy_fm.outlets
            WHERE outlet_id = :outletId
            """, nativeQuery = true)
    OutletLocationProjection getOutletLocation(@Param("outletId") Integer outletId);

    boolean existsByOutletId(Integer outletId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE FmOutlet o
            SET o.isToggle = false
            WHERE o.outletId = :outletId
            """)
    void disableOutlet(Integer outletId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE FmOutlet o
            SET o.isToggle = true
            WHERE o.outletId = :outletId
            """)
    void enableOutlet(Integer outletId);

    @Modifying
    @Query("""
            UPDATE FmOutlet o
            SET o.isActive = :status,
                o.isToggle = false
            WHERE o.outletId = :outletId
            """)
    void permanentlyCloseOutlet(@Param("outletId") Integer outletId, @Param("status") String status);


    //    for fetching outlet name by outlet id to show in order details page and driver app
    @Query(value = """
            SELECT outlet_name
            FROM "jippy_fm"."outlets"
            WHERE outlet_id = :outletId
            """, nativeQuery = true)
    String fetchOutletName(@Param("outletId") Integer outletId);


    @Query(value = """
            SELECT
                o.outlet_id   AS outletId,
                o.outlet_name AS outletName,
                o.outlet_phone AS outletPhone,
                a.area_id     AS areaId,
                ar.area_name  AS areaName
            FROM jippy_fm.outlets o
            JOIN jippy_fm.address a
                ON o.outlet_id = a.jippy_address_id
            JOIN jippy_fm.area ar
                ON a.area_id = ar.area_id
            WHERE a.address_type = 'OUTLET'
            AND o.outlet_id = :outletId
            ORDER BY a.address_id DESC
                    LIMIT 1
            """, nativeQuery = true)
    FmOutletSettlementProjection getOutletDetailsAndAreaAddressForSettlement
            (@Param("outletId") Integer outletId);

    @Query(value = """
            SELECT o.*
            FROM jippy_fm.outlets o
            
            JOIN jippy_fm.address a
            ON o.outlet_id = a.jippy_address_id
            
            WHERE a.area_id = :areaId
            AND o.is_active = 'Y'
            AND o.is_approved = true
            """,
            nativeQuery = true)
    List<FmOutlet> getOutletsByAreaId(Integer areaId);


    // this query checks if an outlet with the same name already exists for the given merchant and area,
// ignoring case and whitespace differences. It returns true if such an outlet exists, otherwise false.
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM jippy_fm.outlets o
                JOIN jippy_fm.address a
                  ON o.outlet_id = a.jippy_address_id
                 AND a.address_type = 'OUTLET'
                WHERE o.merchant_id = :merchantId
                  AND LOWER(TRIM(o.outlet_name)) = LOWER(TRIM(:outletName))
                  AND a.area_id = :areaId
            )
            """, nativeQuery = true)
    boolean existsByMerchantAndOutletNameAndArea(@Param("merchantId") Integer merchantId,
                                                 @Param("outletName") String outletName,
                                                 @Param("areaId") Integer areaId);


    // ---------------------------------FOR -APPROVALS-----------------------------------
    /* Fetch all pending outlets for the logged-in approver.
     *
     * Flow
     *
     * approval_settings
     *      ↓
     * employee
     *      ↓
     * employee address
     *      ↓
     * area
     *      ↓
     * outlet address
     *      ↓
     * outlets
     *
     * Conditions
     *
     * 1. approver_id must match.
     * 2. entity_type must match.
     * 3. Employee address should be EMPLOYEE.
     * 4. Outlet address should be OUTLET.
     * 5. Area Id should be same.
     * 6. Outlet must not be approved.
     * 7. Outlet should be created within last 24 hours.
     */
    @Query(value = """                         
            SELECT
            o.outlet_id              AS outletId,
            o.outlet_name            AS outletName,
            o.merchant_id            AS merchantId,
            o.cuisine_type           AS cuisineType,
            o.outlet_phone           AS outletPhone,
            o.outlet_email           AS outletEmail,
            o.is_approved            AS isApproved,
            o.created_at             AS createdAt
            
            FROM jippy_fm.approval_settings aps
            
            /* Verify Approver Employee */
            
            INNER JOIN jippy_fm.employees emp
            ON emp.employee_id = aps.approver_id
            
            /* Fetch Employee Address */
            
            INNER JOIN jippy_fm.address emp_addr
            ON emp_addr.jippy_address_id = emp.employee_id
            AND emp_addr.address_type='EMPLOYEE'
            
            /* Fetch all Outlet Addresses belonging to same Area */
            
            INNER JOIN jippy_fm.address outlet_addr
            ON outlet_addr.area_id = emp_addr.area_id
            AND outlet_addr.address_type='OUTLET'
            
            /* Fetch Outlets */
            INNER JOIN jippy_fm.outlets o
            ON o.outlet_id = outlet_addr.jippy_address_id
            
            WHERE
            
            aps.approver_id = :approverId
            
            AND aps.entity_type = :entityType
            
            AND aps.is_active = TRUE
            
            AND o.is_approved = FALSE
            
            AND o.created_at >= NOW() - INTERVAL '24 HOURS'
            
            ORDER BY o.created_at DESC""", nativeQuery = true)
    List<FmPendingOutletApprovalProjection> getPendingOutletApprovalRequestsByEntityType(
            @Param("approverId") Integer approverId,
            @Param("entityType") String entityType);

    /**
     * Approve Outlet.
     */
    @Modifying
    @Query("""
            UPDATE FmOutlet
            SET isApproved = true
            WHERE outletId = :outletId
            """)
    int approveOutlet(
            @Param("outletId") Integer outletId);

}


