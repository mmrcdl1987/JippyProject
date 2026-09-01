package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoReorderRequestDto;
import com.jippy.customerandorder.dto.CoReorderResponseDto;
import com.jippy.customerandorder.dto.FmProductDetailResponseDto;
import com.jippy.customerandorder.entity.CoCustomerCart;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderItem;
import com.jippy.customerandorder.exception.CoReorderException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.ICoReorderService;
import com.jippy.customerandorder.mapper.CoReorderMapper;
import com.jippy.customerandorder.repository.CoCustomerCartRepository;
import com.jippy.customerandorder.repository.CoOrderItemRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoReorderServiceImpl implements ICoReorderService {

    private final CoOrderRepository orderRepository;

    private final CoOrderItemRepository orderItemRepository;

    private final CoCustomerCartRepository cartRepository;

    private final FMFeignClient fmFeignClient;

    private final CoReorderMapper reorderMapper;

    @Override
    @Transactional
    public CoReorderResponseDto reorder(CoReorderRequestDto requestDto) {

        log.info("SERVICE_START | REORDER_ORDER | customerId={} | orderId={}", requestDto.getCustomerId(), requestDto.getOrderId());

        CoOrder order = orderRepository.findByOrderIdAndCustomerId(requestDto.getOrderId(), requestDto.getCustomerId()).orElseThrow(() -> {

            log.warn("BUSINESS_VALIDATION_FAILED | ORDER_NOT_FOUND | orderId={} | customerId={}", requestDto.getOrderId(), requestDto.getCustomerId());

            return new CoReorderException("Order not found");
        });

        log.debug("ORDER_FETCHED | orderId={} | outletId={} | status={}", order.getOrderId(), order.getOutletId(), order.getOrderStatus());

        if (!"DELIVERED".equalsIgnoreCase(order.getOrderStatus()) && !"COMPLETED".equalsIgnoreCase(order.getOrderStatus())) {

            log.warn("BUSINESS_VALIDATION_FAILED | INVALID_ORDER_STATUS | orderId={} | status={}", order.getOrderId(), order.getOrderStatus());

            throw new CoReorderException("Only completed orders can be reordered");
        }

        List<CoOrderItem> orderItems = orderItemRepository.findByOrder_OrderId(requestDto.getOrderId());

        if (orderItems.isEmpty()) {

            log.warn("BUSINESS_VALIDATION_FAILED | NO_ORDER_ITEMS_FOUND | orderId={}", requestDto.getOrderId());

            throw new CoReorderException("No order items found");
        }

        log.info("DB_FETCH_SUCCESS | orderId={} | itemCount={}", requestDto.getOrderId(), orderItems.size());

        log.info("DB_OPERATION | CLEAR_CUSTOMER_CART | customerId={}", requestDto.getCustomerId());

        cartRepository.deleteByCustomerId(requestDto.getCustomerId());

        int addedItemsCount = 0;

        List<String> unavailableItems = new ArrayList<>();

        for (CoOrderItem item : orderItems) {

            log.debug("PROCESSING_ORDER_ITEM | productId={} | quantity={}", item.getProductId(), item.getQuantity());

            FmProductDetailResponseDto product;

            try {

                product = fmFeignClient.getProductByIdAndOutletId(item.getProductId(), order.getOutletId());

            } catch (feign.FeignException ex) {

                log.warn("PRODUCT_NOT_AVAILABLE_FOR_OUTLET | productId={} | outletId={}", item.getProductId(), order.getOutletId());

                unavailableItems.add("ProductId-" + item.getProductId());

                continue;
            }

            if (product == null) {

                log.warn("FM_RESPONSE_NULL | productId={}", item.getProductId());

                unavailableItems.add("ProductId-" + item.getProductId());

                continue;
            }

            if (Boolean.FALSE.equals(product.getAvailable())) {

                log.warn("PRODUCT_INACTIVE | productId={} | productName={}", item.getProductId(), product.getProductName());

                unavailableItems.add(product.getProductName());

                continue;
            }

            CoCustomerCart cart = new CoCustomerCart();

            cart.setCustomerId(requestDto.getCustomerId());

            cart.setProductId(item.getProductId());

            cart.setVariantOptionId(item.getVariantOptionId());

            cart.setQuantity(item.getQuantity());

            cart.setTotalPrice(item.getOnlineUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            cart.setCreatedAt(LocalDateTime.now());

            cart.setCreatedBy(requestDto.getCustomerId());

            log.debug("DB_OPERATION | SAVE_CART_ITEM | customerId={} | productId={}", requestDto.getCustomerId(), item.getProductId());

            cartRepository.save(cart);

            addedItemsCount++;

            log.info("PRODUCT_ADDED_TO_CART | productId={} | quantity={}", item.getProductId(), item.getQuantity());
        }

        if (addedItemsCount == 0) {

            log.error("SERVICE_ERROR | NO_PRODUCTS_AVAILABLE_FOR_REORDER | orderId={}", order.getOrderId());

            throw new CoReorderException("No products available for reorder");
        }

        CoReorderResponseDto response = reorderMapper.mapToResponseDto(order.getOrderId(), order.getCustomerId(), addedItemsCount, unavailableItems, "Cart rebuilt successfully");

        log.info("SERVICE_SUCCESS | REORDER_COMPLETED | orderId={} | customerId={} | addedItems={} | unavailableItems={}", order.getOrderId(), order.getCustomerId(), addedItemsCount, unavailableItems.size());

        return response;
    }
}