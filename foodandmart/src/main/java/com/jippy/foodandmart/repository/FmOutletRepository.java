package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.dto.OutletLocationProjection;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.projections.*;
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

    Optional<FmOutlet> findByOutletIdAndIsActive(
            Integer outletId,
            String isActive
    );

    boolean existsByMerchantIdAndOutletName(Integer merchantId, String outletName);

    List<FmOutlet> findByMerchantId(Integer merchantId);


    // for finding the email in te outlet table
    Optional<FmOutlet> findByOutletEmailIgnoreCase(String outletEmail);

    boolean existsByOutletIdAndIsApprovedTrue(Integer outletId);

    //    MANDATORY
//──────────────
//Outlet
//Outlet Category
//Category
//Product
//Outlet Timings
//Product Timings
//
//
//OPTIONAL
//──────────────
//Bank Details
//Address Details
//Cuisine
//Product Variants
//Variant Groups
//    PRICING LOGICS
//1.Online Pricing   -- For Customer these data is mandatory
//2.Variant product + approved online prices exist → use the minimum approved variant online price.
//3.Variant product + no approved online price exists → fall back to the variant merchant price.
//4.Non-variant product + approved online price exists → use the product online price.
//5.Non-variant product + no online price exists → use the product merchant price.
    // ==========================================================================================
    //====================================== FOR CUSTOMER ===================================================
    //=======================================================================================================
        @Query(value = """
                       SELECT
                                   -- Outlet basic details (from outlets table)
                                   o.outlet_id,          -- jippy_fm.outlets
                                   o.outlet_name,        -- jippy_fm.outlets
                                   o.outlet_email,       -- jippy_fm.outlets
                                   o.outlet_phone,       -- jippy_fm.outlets
                                   o.alternate_outlet_phone,   -- jippy_fm.outlets
                    
                                   --- online pricing details (from product_online_pricing table)
                                   --product_id from product_online_pricing table 
                                   (
                                      SELECT MIN(vp.online_price)
                                      FROM jippy_fm.product_online_pricing vp
                                      WHERE vp.product_id = p.product_id
                                        AND vp.outlet_category_id = p.outlet_category_id
                                            AND (
                                              (p.has_product_variants = true
                                               AND vp.product_variant_id IS NOT NULL)
                                               OR
                                               (p.has_product_variants = false
                                               AND vp.product_variant_id IS NULL)
                                               )                                
                                               AND vp.is_approved = true
                                        AND vp.online_price > 0
                                  ) AS online_price,   --product_online_pricing table 
                    
                                   -- Category details (from categories table)
                                   c.category_id,        -- jippy_fm.categories
                                   c.category_name,      -- jippy_fm.categories
                                   oc.is_toggle AS category_available,   -- jippy_fm.categories
                    
                    
                                   -- Product details (from products table)
                                   p.product_id,         -- jippy_fm.products
                                   p.product_name,       -- jippy_fm.products
                                   p.description,        -- jippy_fm.products
                                   
                                   -- Product image URL
                                   p.image_link,         -- jippy_fm.products   
                                   p.merchant_price,     -- jippy_fm.products
                                   p.is_veg,             -- jippy_fm.products
                                   p.has_product_variants, -- jippy_fm.products
                                   p.is_toggle AS product_available,       -- jippy_fm.products
                    
                                   -- =========================================================
                                   -- Product Variant Option Details
                                   -- =========================================================
                                   pvo.product_variant_options_id AS product_variant_id,
                                   pvo.variant_price AS variant_merchant_price,
                                   pvo.price_type AS variant_price_type,
                                   -- Customer online price for this variant
                                   vpop.online_price AS variant_online_price,
                    
                                   -- =========================================================
                                   -- Product Variant Group Value Details
                                   -- =========================================================
                                   pvgv.product_variant_group_values_id AS variant_value_id,
                                   pvgv.variant_name AS variant_name,
                                   pvgv.product_variant_groups_id AS variant_group_id,
                    
                                   -- =========================================================
                                   -- Product Variant Group Details
                                   -- =========================================================
                                   pvg.group_name AS variant_group_name,
                                -- pvg.selection_type AS variant_selection_type,
                                   pvg.min_selection AS variant_min_selection,
                                   pvg.max_selection AS variant_max_selection,
                    
                    
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
                                -- =========================================================
                                -- Product Online Pricing
                                -- this fetches the minimum online price for the product across
                                -- all variants (if any) for the given outlet category.
                                -- =========================================================
                   
                                -- =========================================================
                                -- Product Variant Options
                                -- =========================================================
                              LEFT JOIN jippy_fm.product_variant_options pvo
                                     ON pvo.product_id = p.product_id
                                    AND p.has_product_variants = true
                                    AND pvo.is_active = true
                                    
                               -- =========================================================
                               -- Variant Online Pricing
                               -- Matches one online price to one specific variant
                               -- =========================================================
                    
                               LEFT JOIN jippy_fm.product_online_pricing vpop
                                      ON vpop.product_id = p.product_id
                                     AND vpop.outlet_category_id = p.outlet_category_id
                                     AND vpop.product_variant_id = pvo.product_variant_options_id
                                     AND vpop.is_approved = true 
                                 
                                -- ========================================================
                                -- Product Variant Group Values
                                -- =========================================================
                                LEFT JOIN jippy_fm.product_variant_group_values pvgv
                                       ON pvgv.product_variant_group_values_id =
                                          pvo.product_variant_group_values_id
                                      AND pvgv.is_active = true
        
                                -- =========================================================
                                -- Product Variant Groups
                                -- =========================================================
                                LEFT JOIN jippy_fm.product_variant_groups pvg
                                       ON pvg.product_variant_groups_id =
                                          pvgv.product_variant_groups_id
                                      AND pvg.is_active = true         
                    
                               -- join outlet_days (get outlet timings per day)
                               -- =========================================================
                               -- OUTLET TIMINGS - MANDATORY FOR CUSTOMER
                               -- From: jippy_fm.outlet_days
                               --
                               -- Customer should receive the outlet only when
                               -- outlet timing/day configuration exists.
                               -- =========================================================
                    
                               JOIN jippy_fm.outlet_days od
                                      ON o.outlet_id = od.outlet_id
                    
                               -- Join days_of_week for outlet days (convert day_id to name)
                               -- =========================================================
                               -- OUTLET DAY NAME
                               -- From: jippy_fm.days_of_week
                               --
                               -- Day name is supporting information.
                               -- =========================================================
                    
                               LEFT JOIN jippy_fm.days_of_week d1
                                   ON od.day_of_week_id = d1.day_id
                    
                    
                               -- Left join product_available_timings (get product timing per day)
                               -- Condition ensures product timing matches outlet day
                                JOIN jippy_fm.product_available_timings pat
                                   ON p.product_id = pat.product_id
                                   AND od.day_of_week_id = pat.day_of_week_id
                    
                    
                               -- Join days_of_week for product days (convert day_id to name)
                               LEFT JOIN jippy_fm.days_of_week d2
                                   ON pat.day_of_week_id = d2.day_id
                    
                    
                               -- Filter by outlet_id (input parameter from API)
                    
                                WHERE o.is_approved = true AND o.outlet_id = :outletId AND p.is_toggle= true  --for Api response @query
                               --WHERE o.is_approved = true AND o.outlet_id = 1  --for postgres SQL testing used 
                    
                    
                               -- Order results to simplify grouping in service layer
                               ORDER BY
                                   c.category_id,
                                   p.product_id,
                                   od.day_of_week_id,
                                   pat.start_time
                    """, nativeQuery = true)
        List<FmOutletMenuProjection> getCustomerOutletMenu(@Param("outletId") Integer outletId);
    //==================================================================================================
    //========================================FOR MERCHANT==============================================
    //==================================================================================================
    @Query(
            value = """
                SELECT
        
                -- =========================================================
                -- OUTLET DETAILS
                -- From: jippy_fm.outlets
                -- =========================================================
        
                    o.outlet_id,
                    o.outlet_name,
                    o.outlet_email,
                    o.outlet_phone,
                    o.alternate_outlet_phone,
        
                    -- Outlet availability
                    o.is_toggle AS outlet_available,
        
        
                -- =========================================================
                -- LOCATION DETAILS
                -- From: jippy_fm.outlets.outlet_location
                -- =========================================================
        
                    ST_Y(o.outlet_location::geometry) AS latitude,
                    ST_X(o.outlet_location::geometry) AS longitude,
        
        
                -- =========================================================
                -- BANK DETAILS
                -- From: jippy_fm.user_bank_details
                -- =========================================================
        
                    ubd.account_number,
                    ubd.ifsc_code,
                    ubd.bank_name,
                    ubd.account_holder_name,
        
        
                -- =========================================================
                -- ADDRESS DETAILS
                -- From: jippy_fm.address
                -- =========================================================
        
                    a.building_number,
                    a.road,
                    a.landmark,
                    a.city_id,
                    ct.city_name,
                    a.state_id,
                    st.state_name,
                    a.area_id,
                    ar.area_name,
        
        
                -- =========================================================
                -- CUISINE TYPE DETAILS
                -- From:
                --   jippy_fm.outlets.cuisine_type
                --   jippy_fm.cuisine_types
                --
                -- outlets.cuisine_type contains INTEGER[]
                -- Example: [1, 2]
                --
                -- cuisine_types:
                --   1 -> INDIAN
                --   2 -> CHINESE
                -- =========================================================
        
                    cuisine.cuisine_types_id AS cuisine_type_id,
                    cuisine.cuisine_types_name AS cuisine_type_name,
        
        
                -- =========================================================
                -- CATEGORY DETAILS
                -- From: jippy_fm.categories
                -- =========================================================
        
                    c.category_id,
                    c.category_name,
                    oc.is_toggle AS category_available,
        
        
                -- =========================================================
                -- PRODUCT DETAILS
                -- From: jippy_fm.products
                -- =========================================================
        
                    p.product_id,
                    p.product_name,
                    p.description,
                    p.merchant_price,
                    p.is_veg,
                    p.image_link,
                    p.has_product_variants,
                    p.is_toggle AS product_available,
        
        
                -- =========================================================
                -- PRODUCT VARIANT DETAILS
                -- From: jippy_fm.product_variant_options
                -- =========================================================
        
                    pvo.product_variant_options_id AS product_variant_id,
                    pvo.variant_price AS variant_merchant_price,
                    pvo.price_type AS variant_price_type,
        
        
                -- =========================================================
                -- VARIANT GROUP VALUE DETAILS
                -- From: jippy_fm.product_variant_group_values
                -- =========================================================
        
                    pvgv.product_variant_group_values_id AS variant_value_id,
                    pvgv.variant_name AS variant_name,
                    pvgv.product_variant_groups_id AS variant_group_id,
        
        
                -- =========================================================
                -- VARIANT GROUP DETAILS
                -- From: jippy_fm.product_variant_groups
                -- =========================================================
        
                    pvg.group_name AS variant_group_name,
                    pvg.min_selection AS variant_min_selection,
                    pvg.max_selection AS variant_max_selection,
        
        
                -- =========================================================
                -- OUTLET TIMING DETAILS
                -- From:
                --   jippy_fm.outlet_days
                --   jippy_fm.days_of_week
                -- =========================================================
        
                    od.is_open,
                    od.opening_time,
                    od.closing_time,
                    d1.day_name AS outlet_day,
        
        
                -- =========================================================
                -- PRODUCT TIMING DETAILS
                -- From:
                --   jippy_fm.product_available_timings
                --   jippy_fm.days_of_week
                -- =========================================================
        
                    pat.start_time,
                    pat.end_time,
                    d2.day_name AS product_day
        
        
                -- =========================================================
                -- MAIN TABLE
                -- From: jippy_fm.outlets
                -- =========================================================
        
                FROM jippy_fm.outlets o
        
        
                -- =========================================================
                -- OUTLET BANK DETAILS
                -- From: jippy_fm.user_bank_details
                -- =========================================================
        
                LEFT JOIN jippy_fm.user_bank_details ubd
                       ON ubd.recipient_id = o.outlet_id
                      AND ubd.user_type = 'OUTLET'
        
        
                -- =========================================================
                -- OUTLET ADDRESS
                -- From: jippy_fm.address
                -- =========================================================
        
                LEFT JOIN jippy_fm.address a
                       ON a.jippy_address_id = o.outlet_id
                      AND a.address_type = 'OUTLET'
        
        
                -- =========================================================
                -- STATE
                -- From: jippy_fm.state
                -- =========================================================
        
                LEFT JOIN jippy_fm.state st
                       ON st.state_id = a.state_id
        
        
                -- =========================================================
                -- CITY
                -- From: jippy_fm.city
                -- =========================================================
        
                LEFT JOIN jippy_fm.city ct
                       ON ct.city_id = a.city_id
        
        
                -- =========================================================
                -- AREA
                -- From: jippy_fm.area
                -- =========================================================
        
                LEFT JOIN jippy_fm.area ar
                       ON ar.area_id = a.area_id
        
        
                -- =========================================================
                -- CUISINE TYPES
                -- From: jippy_fm.cuisine_types
                --
                -- o.cuisine_type is INTEGER[]
                -- ANY() matches cuisine_types_id
                -- with the IDs stored in the array.
                -- =========================================================
        
                LEFT JOIN jippy_fm.cuisine_types cuisine
                       ON cuisine.cuisine_types_id = ANY(o.cuisine_type)
        
        
                -- =========================================================
                -- OUTLET CATEGORIES
                -- From: jippy_fm.outlet_categories
                -- =========================================================
        
                LEFT JOIN jippy_fm.outlet_categories oc
                       ON o.outlet_id = oc.outlet_id
        
        
                -- =========================================================
                -- CATEGORIES
                -- From: jippy_fm.categories
                -- =========================================================
        
                LEFT JOIN jippy_fm.categories c
                       ON oc.category_id = c.category_id
        
        
                -- =========================================================
                -- PRODUCTS
                -- From: jippy_fm.products
                -- =========================================================
        
                LEFT JOIN jippy_fm.products p
                       ON oc.outlet_category_id = p.outlet_category_id
        
        
                -- =========================================================
                -- PRODUCT VARIANTS
                -- From: jippy_fm.product_variant_options
                --
                -- Only active variants are included.
                -- =========================================================
        
                LEFT JOIN jippy_fm.product_variant_options pvo
                       ON pvo.product_id = p.product_id
                      AND p.has_product_variants = true
                      AND pvo.is_active = true
        
        
                -- =========================================================
                -- VARIANT GROUP VALUES
                -- From: jippy_fm.product_variant_group_values
                --
                -- Only active group values are included.
                -- =========================================================
        
                LEFT JOIN jippy_fm.product_variant_group_values pvgv
                       ON pvgv.product_variant_group_values_id =
                          pvo.product_variant_group_values_id
                      AND pvgv.is_active = true
        
        
                -- =========================================================
                -- VARIANT GROUP
                -- From: jippy_fm.product_variant_groups
                --
                -- Only active groups are included.
                -- =========================================================
        
                LEFT JOIN jippy_fm.product_variant_groups pvg
                       ON pvg.product_variant_groups_id =
                          pvgv.product_variant_groups_id
                      AND pvg.is_active = true
        
        
                -- =========================================================
                -- OUTLET DAYS
                -- From: jippy_fm.outlet_days
                -- =========================================================
        
                LEFT JOIN jippy_fm.outlet_days od
                       ON o.outlet_id = od.outlet_id
        
        
                -- =========================================================
                -- OUTLET DAY NAME
                -- From: jippy_fm.days_of_week
                -- =========================================================
        
                LEFT JOIN jippy_fm.days_of_week d1
                       ON od.day_of_week_id = d1.day_id
        
        
                -- =========================================================
                -- PRODUCT AVAILABLE TIMINGS
                -- From: jippy_fm.product_available_timings
                --
                -- Matches product timing with outlet day.
                -- =========================================================
        
                LEFT JOIN jippy_fm.product_available_timings pat
                       ON p.product_id = pat.product_id
                      AND od.day_of_week_id = pat.day_of_week_id
        
        
                -- =========================================================
                -- PRODUCT DAY NAME
                -- From: jippy_fm.days_of_week
                -- =========================================================
        
                LEFT JOIN jippy_fm.days_of_week d2
                       ON pat.day_of_week_id = d2.day_id
        
        
                -- =========================================================
                -- FILTER
                -- Only approved outlet with requested outlet ID.
                -- =========================================================
        
                WHERE o.is_approved = true
                  AND o.outlet_id = :outletId
        
        
                -- =========================================================
                -- ORDER
                -- Category → Product → Outlet Day → Product Timing
                -- =========================================================
        
                ORDER BY
                    cuisine.cuisine_types_id,
                    c.category_id,
                    p.product_id,
                    od.day_of_week_id,
                    pat.start_time
                """,
            nativeQuery = true
    )
    List<FmMerchantOutletMenuProjection> getMerchantOutletMenu(@Param("outletId") Integer outletId);
//=====================================================================================================
//    =================================================================================================

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
                (
                    SELECT STRING_AGG(ct.cuisine_types_name, ', ')
                    FROM jippy_fm.cuisine_types ct
                    WHERE ct.cuisine_types_id = ANY(o.cuisine_type)
                ) AS cuisine_names,
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
            
                ST_Y(o.outlet_location::geometry) AS latitude,
                ST_X(o.outlet_location::geometry) AS longitude,
                o.is_veg_outlet,
                o.outlet_pic_url,
                EXISTS (
                    SELECT 1 
                    FROM jippy_fm.best_restaurants br 
                    WHERE br.outlet_id = o.outlet_id
                ) AS is_best_restaurant
            
            FROM jippy_fm.outlets o
            
            LEFT JOIN jippy_fm.outlet_subscription_plans osp
                   ON o.outlet_id = osp.outlet_id
            
            LEFT JOIN jippy_fm.week_slot_days wsd 
                   ON wsd.week_slot_days_id = osp.banner_slot_days_id
                  AND CURRENT_DATE BETWEEN wsd.slot_start_date AND wsd.slot_end_date 
                  AND slot_type ='banner_slot'
            
            LEFT JOIN jippy_fm.subscription_plans sp
                   ON osp.subscription_plan_id = sp.subscription_plan_id
            
            JOIN jippy_fm.outlet_days od
                   ON od.outlet_id = o.outlet_id
            
            JOIN jippy_fm.days_of_week dow
                   ON dow.day_id = od.day_of_week_id
            
            WHERE o.is_active = 'Y'
              AND o.outlet_location IS NOT NULL
              AND o.is_approved = true
            
              -- Spatial filter
              AND ST_DWithin(
                    o.outlet_location::geography,
                    ST_SetSRID(
                        ST_MakePoint(:customerLng, :customerLat),
                        4326
                    )::geography,
                    COALESCE(sp.radius_in_kms * 1000, 3000)
                  )
            
              -- Outlet Day & Time filter
              AND od.day_of_week_id = EXTRACT(ISODOW FROM (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Kolkata'))
              AND (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Kolkata')::time BETWEEN od.opening_time AND od.closing_time
            
              -- Product timing check without duplicating rows
              AND EXISTS (
                  SELECT 1
                  FROM jippy_fm.outlet_categories oc
                  JOIN jippy_fm.products p 
                    ON p.outlet_category_id = oc.outlet_category_id
                  JOIN jippy_fm.product_available_timings pat 
                    ON p.product_id = pat.product_id 
                   AND pat.day_of_week_id = od.day_of_week_id
                  WHERE oc.outlet_id = o.outlet_id
                    AND (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Kolkata')::time BETWEEN pat.start_time AND pat.end_time 
                    AND o.is_approved = true AND o.is_toggle = true AND (:categoryId IS NULL OR oc.category_id = :categoryId)
              )
            
            ORDER BY distance_km ASC;
            """, nativeQuery = true)
    List<Object[]> findCustomerNearbyOutlets(@Param("customerLat") double customerLat,
                                             @Param("customerLng") double customerLng, @Param("categoryId") Integer categoryId);

    @Query(value = """
            SELECT DISTINCT
                o.outlet_id AS outletId,
                o.outlet_name AS outletName,
                o.merchant_id AS merchantId,
                o.review AS rating,
                CASE WHEN o.is_active = 'Y' THEN true ELSE false END AS isActive,
                o.is_approved AS isApproved,
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
                ) AS distanceKm,
                CASE WHEN EXISTS (
                    SELECT 1
                    FROM jippy_fm.outlet_days od
                    WHERE od.outlet_id = o.outlet_id
                      AND od.day_of_week_id = EXTRACT(ISODOW FROM (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Kolkata'))
                      AND (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Kolkata')::time BETWEEN od.opening_time AND od.closing_time
                ) THEN true ELSE false END AS openNow,
                o.is_veg_outlet AS isVegOutlet,
                o.outlet_pic_url AS outletPicUrl
            FROM jippy_fm.outlets o
            LEFT JOIN jippy_fm.outlet_subscription_plans osp
                   ON osp.outlet_id = o.outlet_id
            LEFT JOIN jippy_fm.subscription_plans sp
                   ON sp.subscription_plan_id = osp.subscription_plan_id
            WHERE o.is_active = 'Y'
              AND o.outlet_location IS NOT NULL
              AND o.is_approved = true
              AND ST_DWithin(
                    o.outlet_location::geography,
                    ST_SetSRID(
                        ST_MakePoint(:customerLng, :customerLat),
                        4326
                    )::geography,
                    COALESCE(sp.radius_in_kms * 1000, 10000)
                  )
            ORDER BY distanceKm ASC
            """, nativeQuery = true)
    List<com.jippy.foodandmart.projections.FmPublicCustomerNearbyOutletProjection> fetchPublicCustomerNearbyOutlets(
            @Param("customerLat") double customerLat,
            @Param("customerLng") double customerLng);



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
    List<FmOutlet> getOutletsByAreaId(
            @Param("areaId") Integer areaId
    );

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


    @Query(value = """
            SELECT o.outlet_id,a.state_id,a.city_id,a.area_id FROM "jippy_fm"."outlets" o
            join "jippy_fm"."address" a on  o.outlet_id = a.jippy_address_id and address_type = 'OUTLET'
            where outlet_id =:outletId """,nativeQuery = true)
    OutletAddressProjection getOutletAddressDetails(@Param("outletId") Integer outletId);


    @Query(value = """
        SELECT

            -- =========================================================
            -- OUTLET
            -- =========================================================
            o.outlet_id,
            o.outlet_name,
            o.outlet_email,
            o.outlet_phone,
            o.alternate_outlet_phone,

            o.is_active,
            o.is_approved,
            o.is_toggle AS outlet_available,
            o.is_toggle AS outlet_toggle,
            o.is_gst_applied AS gst_applied,

            ST_Y(o.outlet_location::geometry) AS latitude,
            ST_X(o.outlet_location::geometry) AS longitude,

            -- =========================================================
            -- BANK
            -- =========================================================
            ubd.account_number,
            ubd.ifsc_code,
            ubd.bank_name,
            ubd.account_holder_name,

            -- =========================================================
            -- ADDRESS
            -- =========================================================
            a.building_number,
            a.road,
            a.landmark,

            a.city_id,
            ct.city_name,

            a.state_id,
            st.state_name,

            a.area_id,
            ar.area_name,

            -- =========================================================
            -- CUISINE
            -- =========================================================
            cuisine.cuisine_types_id AS cuisine_type_id,
            cuisine.cuisine_types_name AS cuisine_type_name,

            -- =========================================================
            -- CATEGORY
            -- =========================================================
            c.category_id,
            c.category_name,
            oc.is_toggle AS category_available,
            oc.is_toggle AS category_toggle,

            -- =========================================================
            -- PRODUCT
            -- =========================================================
            p.product_id,
            p.product_name,
            p.description,
            p.image_link,
            p.merchant_price,
            p.is_veg,
            p.has_product_variants,
            p.is_toggle AS product_available,
            p.is_toggle AS product_toggle,

            -- =========================================================
            -- PRODUCT ONLINE PRICE
            --
            -- Non-variant product:
            -- product_variant_id IS NULL
            --
            -- Variant product:
            -- minimum online price among active configured variants
            -- =========================================================
            (
                SELECT MIN(pop.online_price)
                FROM jippy_fm.product_online_pricing pop
                WHERE pop.product_id = p.product_id
                  AND pop.outlet_category_id = p.outlet_category_id
                  AND pop.online_price > 0
                  AND (
                        (
                            p.has_product_variants = true
                            AND pop.product_variant_id IS NOT NULL
                        )
                        OR
                        (
                            p.has_product_variants = false
                            AND pop.product_variant_id IS NULL
                        )
                  )
            ) AS online_price,

            -- =========================================================
            -- VARIANT OPTION
            -- =========================================================
            pvo.product_variant_options_id AS product_variant_id,
            pvo.variant_price AS variant_merchant_price,
            pvo.price_type AS variant_price_type,

            -- =========================================================
            -- VARIANT ONLINE PRICE
            -- =========================================================
            vpop.online_price AS variant_online_price,

            -- =========================================================
            -- VARIANT VALUE
            -- =========================================================
            pvgv.product_variant_group_values_id AS variant_value_id,
            pvgv.variant_name,
            pvgv.product_variant_groups_id AS variant_group_id,

            -- =========================================================
            -- VARIANT GROUP
            -- =========================================================
            pvg.group_name AS variant_group_name,

            -- =========================================================
            -- OUTLET TIMINGS
            -- =========================================================
            od.is_open,
            od.opening_time,
            od.closing_time,
            d1.day_name AS outlet_day,

            -- =========================================================
            -- PRODUCT TIMINGS
            -- =========================================================
            pat.start_time,
            pat.end_time,
            d2.day_name AS product_day

        FROM jippy_fm.outlets o

        -- =========================================================
        -- BANK
        -- =========================================================
        LEFT JOIN jippy_fm.user_bank_details ubd
               ON ubd.recipient_id = o.outlet_id
              AND ubd.user_type = 'OUTLET'

        -- =========================================================
        -- ADDRESS
        -- =========================================================
        LEFT JOIN jippy_fm.address a
               ON a.jippy_address_id = o.outlet_id
              AND a.address_type = 'OUTLET'

        LEFT JOIN jippy_fm.state st
               ON st.state_id = a.state_id

        LEFT JOIN jippy_fm.city ct
               ON ct.city_id = a.city_id

        LEFT JOIN jippy_fm.area ar
               ON ar.area_id = a.area_id

        -- =========================================================
        -- CUISINE
        -- =========================================================
        LEFT JOIN jippy_fm.cuisine_types cuisine
               ON cuisine.cuisine_types_id = ANY(o.cuisine_type)

        -- =========================================================
        -- OUTLET CATEGORY
        -- =========================================================
        LEFT JOIN jippy_fm.outlet_categories oc
               ON o.outlet_id = oc.outlet_id

        LEFT JOIN jippy_fm.categories c
               ON oc.category_id = c.category_id

        -- =========================================================
        -- PRODUCT
        -- =========================================================
        LEFT JOIN jippy_fm.products p
               ON oc.outlet_category_id = p.outlet_category_id

        -- =========================================================
        -- PRODUCT VARIANT OPTIONS
        -- =========================================================
        LEFT JOIN jippy_fm.product_variant_options pvo
               ON pvo.product_id = p.product_id
              AND p.has_product_variants = true
              AND pvo.is_active = true

        -- =========================================================
        -- VARIANT ONLINE PRICING
        --
        -- product_online_pricing.product_variant_id
        -- matches product_variant_options.product_variant_options_id
        --
        -- Admin sees configured pricing regardless of approval status.
        -- =========================================================
        LEFT JOIN jippy_fm.product_online_pricing vpop
               ON vpop.product_id = p.product_id
              AND vpop.outlet_category_id = p.outlet_category_id
              AND vpop.product_variant_id = pvo.product_variant_options_id
              AND vpop.online_price > 0

        -- =========================================================
        -- VARIANT GROUP VALUE
        -- =========================================================
        LEFT JOIN jippy_fm.product_variant_group_values pvgv
               ON pvgv.product_variant_group_values_id =
                  pvo.product_variant_group_values_id
              AND pvgv.is_active = true

        -- =========================================================
        -- VARIANT GROUP
        -- =========================================================
        LEFT JOIN jippy_fm.product_variant_groups pvg
               ON pvg.product_variant_groups_id =
                  pvgv.product_variant_groups_id
              AND pvg.is_active = true

        -- =========================================================
        -- OUTLET DAYS
        -- =========================================================
        LEFT JOIN jippy_fm.outlet_days od
               ON o.outlet_id = od.outlet_id

        LEFT JOIN jippy_fm.days_of_week d1
               ON od.day_of_week_id = d1.day_id

        -- =========================================================
        -- PRODUCT AVAILABLE TIMINGS
        -- =========================================================
        LEFT JOIN jippy_fm.product_available_timings pat
               ON p.product_id = pat.product_id
              AND od.day_of_week_id = pat.day_of_week_id

        LEFT JOIN jippy_fm.days_of_week d2
               ON pat.day_of_week_id = d2.day_id

        -- =========================================================
        -- OUTLET FILTER
        -- =========================================================
        WHERE o.outlet_id = :outletId

        ORDER BY
            cuisine.cuisine_types_id,
            c.category_id,
            p.product_id,
            pvg.product_variant_groups_id,
            pvo.product_variant_options_id,
            od.day_of_week_id,
            pat.start_time
        """, nativeQuery = true)
    List<FmAdminOutletMenuProjection> getAdminOutletMenu(
            @Param("outletId") Integer outletId
    );

    @Query(value = """
            SELECT
                -- OUTLET DETAILS
                o.outlet_id,
                o.outlet_name,
                o.is_toggle AS outlet_available,

                -- CATEGORY DETAILS
                c.category_id,
                c.category_name,
                oc.is_toggle AS category_available,

                -- PRODUCT DETAILS
                p.product_id,
                p.product_name,
                p.description,
                -- Online price from jippy_fm.product_online_pricing
                (
                    SELECT MIN(pop.online_price)
                    FROM jippy_fm.product_online_pricing pop
                    WHERE pop.product_id = p.product_id
                      AND pop.outlet_category_id = p.outlet_category_id
                      AND pop.online_price > 0
                      AND (
                            (
                                p.has_product_variants = true
                                AND pop.product_variant_id IS NOT NULL
                            )
                            OR
                            (
                                p.has_product_variants = false
                                AND pop.product_variant_id IS NULL
                            )
                      )
                ) AS online_price,
                p.is_veg,
                p.image_link,
                p.has_product_variants,
                p.is_toggle AS product_available,

                -- PRODUCT VARIANT DETAILS
                pvo.product_variant_options_id AS product_variant_id,
                pvo.variant_price AS variant_merchant_price,
                pvo.price_type AS variant_price_type,

                -- VARIANT GROUP VALUE DETAILS
                pvgv.product_variant_group_values_id AS variant_value_id,
                pvgv.variant_name AS variant_name,
                pvgv.product_variant_groups_id AS variant_group_id,

                -- VARIANT GROUP DETAILS
                pvg.group_name AS variant_group_name,
                pvg.min_selection AS variant_min_selection,
                pvg.max_selection AS variant_max_selection

            FROM jippy_fm.outlets o

            -- OUTLET CATEGORIES
            LEFT JOIN jippy_fm.outlet_categories oc
                   ON o.outlet_id = oc.outlet_id

            -- CATEGORIES
            LEFT JOIN jippy_fm.categories c
                   ON oc.category_id = c.category_id

            -- PRODUCTS
            LEFT JOIN jippy_fm.products p
                   ON oc.outlet_category_id = p.outlet_category_id

            -- PRODUCT VARIANTS
            LEFT JOIN jippy_fm.product_variant_options pvo
                   ON pvo.product_id = p.product_id
                  AND p.has_product_variants = true
                  AND pvo.is_active = true

            -- VARIANT GROUP VALUES
            LEFT JOIN jippy_fm.product_variant_group_values pvgv
                   ON pvgv.product_variant_group_values_id =
                      pvo.product_variant_group_values_id
                  AND pvgv.is_active = true

            -- VARIANT GROUP
            LEFT JOIN jippy_fm.product_variant_groups pvg
                   ON pvg.product_variant_groups_id =
                      pvgv.product_variant_groups_id
                  AND pvg.is_active = true

            -- FILTER
            WHERE o.is_approved = true
              AND o.outlet_id = :outletId

            ORDER BY
                c.category_id,
                p.product_id,
                pvg.product_variant_groups_id,
                pvo.product_variant_options_id
            """, nativeQuery = true)
    List<FmPublicOutletDetailsProjection> getPublicOutletDetails(
            @Param("outletId") Integer outletId
    );

//    =================================================================================
    // ================================================================
// UPDATE OUTLET TOGGLE VALUE
// ================================================================
//
// Updates only the is_toggle column for the specified outlet.
//
// outletId  -> Identifies the outlet
// isToggle  -> New value (true / false)
//
// Returns:
// 1 -> record updated successfully
// 0 -> outlet not found
// ================================================================

    @Modifying
    @Query("""
        UPDATE FmOutlet o
        SET o.isToggle = :isToggle
        WHERE o.outletId = :outletId
        """)
    int updateOutletToggle(
            @Param("outletId") Integer outletId,
            @Param("isToggle") Boolean isToggle
    );

}