package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import com.jippy.customerandorder.projection.CoOrderPriceBreakupProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoOrderPriceBreakupRepository
        extends JpaRepository<CoOrderPriceBreakup, Integer> {

    /**
     * Fetch price breakup using order ID.
     */
    CoOrderPriceBreakup findByOrder_OrderId(String orderId);

//    ==================================================================================
//    ==================================================================================
@Query(value = """
          SELECT
                opb.order_id AS "orderId",
                opb.order_amount AS "orderAmount",
                    
                opb.pick_up_distance_in_kms AS "pickUpDistanceInKms",
                opb.delivery_distance_in_kms AS "deliveryDistanceInKms",

                opb.pick_up_charges AS "pickUpCharges",
                opb.driver_delivery_fee AS "driverDeliveryFee",
                opb.customer_delivery_fee AS "customerDeliveryFee",
                opb.total_delivery_fee AS "totalDeliveryFee",

                opb.platform_fee AS "platformFee",
                opb.platform_fee_tax AS "platformFeeTax",

                opb.surge_fee AS "surgeFee",
                opb.surge_fee_tax AS "surgeFeeTax",

                opb.packaging_fee AS "packagingFee",
                opb.packaging_fee_tax AS "packagingFeeTax",

                opb.food_tax AS "foodTax",
                opb.total_tax AS "totalTax",

                opb.tip AS "tip",
                opb.coupon_discount AS "couponDiscount",
                opb.wallet_amount AS "walletAmount",

                opb.order_amount_discounted AS "orderAmountDiscounted",
                opb.order_total_amount AS "orderTotalAmount",

                opb.customer_delivery_fee_tax AS "customerDeliveryFeeTax"

            FROM jippy_customer_and_order.order_price_breakup opb

            WHERE opb.order_id = :orderId
            """,
        nativeQuery = true)
Optional<CoOrderPriceBreakupProjection> getPriceBreakup(
        @Param("orderId") String orderId
);
}