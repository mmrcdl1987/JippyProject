package com.jippy.division.repositary;

import com.jippy.division.entity.DivPriceDropMappingOutletProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivPriceDropMappingRepository extends JpaRepository<DivPriceDropMappingOutletProduct, Integer> {
    @Query(value = """
        SELECT DISTINCT pdmop.outlet_id
        FROM jippy_division.price_drop_mapping_outlets_products pdmop

        JOIN jippy_division.promotion_time pt
        ON pdmop.promotion_time_id =
        pt.promotion_time_id

        JOIN jippy_division.promotion_date pd
        ON pt.promotion_date_id =
        pd.promotion_date_id

        WHERE CURRENT_DATE BETWEEN
        CAST(pd.promotion_from_date AS DATE)
        AND CAST(pd.promotion_to_date AS DATE)

        AND CURRENT_TIME BETWEEN
        CAST(pt.promotion_from_time AS TIME)
        AND CAST(pt.promotion_to_time AS TIME)
        """,
            nativeQuery = true)
    List<Integer> findActivePriceDropOutlets();
}