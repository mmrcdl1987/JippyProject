package com.jippy.customerandorder.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CO Projection for merchant settlement order details.
 *
 * This projection fetches only the data that belongs to the
 * Customer & Order (CO) service.
 *
 * Data is fetched from:
 *
 * 1. jippy_customer_and_order.orders
 * 2. jippy_customer_and_order.order_items
 * 3. jippy_customer_and_order.order_price_breakup
 *
 * FM-related information such as:
 *
 * - Product name
 * - Variant name
 * - GST applicability
 * - GST percentage
 *
 * will be handled by the FM service.
 *
 * One order can contain multiple order items.
 * Therefore, this projection can return multiple records
 * for the same orderId.
 */
public interface CoMerchantSettlementOrderDetailsProjection {

    /**
     * Order ID from CO orders table.
     *
     * Example:
     * jippy202609193
     */
    String getOrderId();

    /**
     * Order creation date and time from CO orders table.
     *
     * This value is used for checking whether the order
     * falls within the requested settlement period.
     */
    LocalDateTime getCreatedAt();

    /**
     * Product ID from CO order_items table.
     *
     * FM service will use this ID to fetch the
     * corresponding product name from FM products table.
     */
    Integer getProductId();

    /**
     * Variant option ID from CO order_items table.
     *
     * FM service will use this ID to find the corresponding
     * variant name through:
     *
     * product_variant_options
     *          ↓
     * product_variant_group_values
     */
    Integer getVariantOptionId();

    /**
     * Quantity of the product ordered.
     *
     * Example:
     * 2
     */
    Integer getQuantity();

    /**
     * Merchant total price from CO order_items table.
     *
     * This is the merchant-side total amount before
     * applying the merchant promotion deduction.
     *
     * Example:
     * 758.00
     */
    BigDecimal getMerchantTotalPrice();

    /**
     * Promotion amount associated with the order.
     *
     * This value should be considered as a merchant promotion
     * deduction only when discountType is:
     *
     * MERCHANT_PROMOTION
     *
     * Otherwise, the service logic can treat this value
     * as zero.
     */
    BigDecimal getPromotionDeductedAmount();

    /**
     * Discount type from CO order_price_breakup table.
     *
     * Example:
     * MERCHANT_PROMOTION
     *
     * This field is used to determine whether the promotion
     * deduction should be applied to the merchant amount.
     */
    String getDiscountType();
}