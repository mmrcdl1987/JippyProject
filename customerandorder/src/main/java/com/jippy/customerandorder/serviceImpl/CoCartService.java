package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.Constants.COConstants;
import com.jippy.customerandorder.dto.CoCartItemResponseDto;
import com.jippy.customerandorder.dto.CoCartResponseDto;
import com.jippy.customerandorder.dto.CoCartUpdateRequestDto;
import com.jippy.customerandorder.dto.FmProductDetailResponseDto;
import com.jippy.customerandorder.entity.CoCustomerCart;
import com.jippy.customerandorder.exception.CartException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.ICartService;
import com.jippy.customerandorder.repository.CoCustomerCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoCartService implements ICartService {

    private final CoCustomerCartRepository cartRepository;
    private final FMFeignClient productFeignClient;


    public String updateCart(CoCartUpdateRequestDto dto) {

        log.info("Cart Update START | customerId={}, productId={}, qty={}, unitPrice={}",
                dto.getCustomerId(), dto.getProductId(), dto.getQuantity(), dto.getUnitPrice());

        // VALIDATE INPUT
        if (dto.getCustomerId() == null || dto.getCustomerId() <= 0) {
            log.warn("Invalid customer ID | customerId={}", dto.getCustomerId());
            throw new CartException("Invalid customer ID");
        }

        if (dto.getProductId() == null || dto.getProductId() <= 0) {
            log.warn("Invalid product ID | productId={}", dto.getProductId());
            throw new CartException("Invalid product ID");
        }

        if (dto.getUnitPrice() == null || dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Invalid unit price | unitPrice={}", dto.getUnitPrice());
            throw new CartException("Unit price cannot be negative");
        }

        // VALIDATION - QUANTITY
        if (dto.getQuantity() < 0) {
            log.warn("Invalid quantity provided | quantity={}", dto.getQuantity());
            throw new CartException(COConstants.MSG_INVALID_QUANTITY);
        }

        // CHECK EXISTING CART
        CoCustomerCart cart;

        try {
            cart = cartRepository
                    .findByCustomerIdAndProductId(dto.getCustomerId(), dto.getProductId())
                    .orElse(null);
            log.debug("Existing cart item lookup | exists={}, customerId={}, productId={}", 
                    cart != null, dto.getCustomerId(), dto.getProductId());
        } catch (Exception ex) {
            log.error("Database error while checking existing cart | customerId={}, productId={}", 
                    dto.getCustomerId(), dto.getProductId(), ex);
            throw new CartException("Unable to check cart");
        }

        //  REMOVE ITEM (quantity = 0)
        if (dto.getQuantity() == 0) {

            if (cart != null) {
                try {
                    cartRepository.delete(cart);
                    log.info("Cart item removed successfully | cartId={}, customerId={}, productId={}", 
                            cart.getCartId(), dto.getCustomerId(), dto.getProductId());
                } catch (Exception ex) {
                    log.error("Database error while deleting cart item | cartId={}", cart.getCartId(), ex);
                    throw new CartException("Unable to remove cart item");
                }
            } else {
                log.warn("Attempted to remove non-existent cart item | customerId={}, productId={}",
                        dto.getCustomerId(), dto.getProductId());
            }

            log.debug("Cart item removal process completed | customerId={}", dto.getCustomerId());
            return COConstants.MSG_CART_REMOVED;
        }

        //  CALCULATE TOTAL PRICE
        BigDecimal totalPrice = dto.getUnitPrice()
                .multiply(BigDecimal.valueOf(dto.getQuantity()));

        log.debug("Calculated total price | unitPrice={}, quantity={}, totalPrice={}",
                dto.getUnitPrice(), dto.getQuantity(), totalPrice);

        //  UPDATE EXISTING CART
        if (cart != null) {

            int previousQty = cart.getQuantity();
            BigDecimal previousTotal = cart.getTotalPrice();

            cart.setQuantity(dto.getQuantity());
            cart.setTotalPrice(totalPrice);
            cart.setUpdatedAt(LocalDateTime.now());
            cart.setUpdatedBy(1);

            try {
                cartRepository.save(cart);
                log.info("Cart UPDATED successfully | cartId={}, customerId={}, productId={}, " +
                                "qty: {} -> {}, totalPrice: {} -> {}",
                        cart.getCartId(), dto.getCustomerId(), dto.getProductId(),
                        previousQty, dto.getQuantity(), previousTotal, totalPrice);
            } catch (Exception ex) {
                log.error("Database error while updating cart | cartId={}", cart.getCartId(), ex);
                throw new CartException("Unable to update cart");
            }

            return COConstants.MSG_CART_UPDATED;
        }

        //  CREATE NEW CART ITEM
        CoCustomerCart newCart = new CoCustomerCart();
        newCart.setCustomerId(dto.getCustomerId());
        newCart.setProductId(dto.getProductId());
        newCart.setQuantity(dto.getQuantity());
        newCart.setTotalPrice(totalPrice);
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setCreatedBy(1);

        try {
            cartRepository.save(newCart);
            log.info("Cart CREATED successfully | customerId={}, productId={}, qty={}, totalPrice={}",
                    dto.getCustomerId(), dto.getProductId(), dto.getQuantity(), totalPrice);
        } catch (Exception ex) {
            log.error("Database error while creating new cart | customerId={}, productId={}", 
                    dto.getCustomerId(), dto.getProductId(), ex);
            throw new CartException("Unable to add item to cart");
        }

        return COConstants.MSG_CART_ADDED;
    }

    public CoCartResponseDto getCart(Integer customerId) {

        log.info("GET CART START | customerId={}", customerId);

        // VALIDATE INPUT
        if (customerId == null || customerId <= 0) {
            log.warn("Invalid customer ID provided | customerId={}", customerId);
            throw new CartException("Invalid customer ID");
        }

        log.debug("Customer ID validation passed | customerId={}", customerId);

        // FETCH CART ITEMS FROM DATABASE
        log.debug("Querying database for cart items | customerId={}", customerId);

        List<CoCustomerCart> cartList =
                cartRepository.findByCustomerId(customerId);

        log.debug("Cart items fetched from database | count={}, customerId={}",
                cartList.size(), customerId);

        if (cartList.isEmpty()) {
            log.warn("Cart is empty for customer | customerId={}", customerId);
            throw new CartException("Cart is empty");
        }

        log.debug("Cart has items | customerId={}, itemCount={}", customerId, cartList.size());

        List<CoCartItemResponseDto> items = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        log.debug("Starting to process cart items | totalItems={}, customerId={}",
                cartList.size(), customerId);

        // PROCESS EACH CART ITEM
        for (CoCustomerCart cart : cartList) {

            log.debug("Processing cart item | cartId={}, productId={}, quantity={}, customerId={}",
                    cart.getCartId(), cart.getProductId(), cart.getQuantity(), customerId);

            FmProductDetailResponseDto product;

            try {
                log.debug("Fetching product details from food service | productId={}, customerId={}",
                        cart.getProductId(), customerId);
                product = productFeignClient.getProductById(cart.getProductId());
                log.debug("Product fetched successfully | productId={}, customerId={}",
                        cart.getProductId(), customerId);
            } catch (Exception ex) {
                log.error("Feign call failed while fetching product | productId={}, customerId={}, error={}",
                        cart.getProductId(), customerId, ex.getMessage(), ex);
                throw new CartException("Unable to fetch product details");
            }

            if (product == null) {
                log.error("Product response is null from food service | productId={}, customerId={}",
                        cart.getProductId(), customerId);
                throw new CartException("Product not found");
            }

            log.debug("Product validation passed | productId={}, productName={}, customerId={}",
                    product.getProductId(), product.getProductName(), customerId);

            // MAP TO RESPONSE DTO
            CoCartItemResponseDto item = new CoCartItemResponseDto();

            item.setProductId(cart.getProductId());
            item.setProductName(product.getProductName());
            item.setProductImage(product.getProductImage());
            item.setQuantity(cart.getQuantity());
            item.setTotalPrice(cart.getTotalPrice());

            items.add(item);

            grandTotal = grandTotal.add(cart.getTotalPrice());

            log.debug("Cart item processed successfully | productId={}, quantity={}, itemTotal={}, customerId={}",
                    cart.getProductId(), cart.getQuantity(), cart.getTotalPrice(), customerId);
        }

        log.debug("All cart items processed successfully | itemCount={}, customerId={}",
                items.size(), customerId);

        // BUILD RESPONSE
        CoCartResponseDto response = new CoCartResponseDto();
        response.setCustomerId(customerId);
        response.setItems(items);
        response.setGrandTotal(grandTotal);

        log.info("GET CART SUCCESS | customerId={}, itemCount={}, grandTotal={}",
                customerId, items.size(), grandTotal);

        return response;
    }
}