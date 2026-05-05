package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.Constants.COConstants;
import com.jippy.customerandorder.dto.CoCartUpdateRequestDto;
import com.jippy.customerandorder.entity.CoCustomerCart;
import com.jippy.customerandorder.exception.CartException;
import com.jippy.customerandorder.iservice.ICartService;
import com.jippy.customerandorder.repository.CoCustomerCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoCartService implements ICartService {

    private final CoCustomerCartRepository cartRepository;

    public String updateCart(CoCartUpdateRequestDto dto) {

        log.info("Cart Update START | customerId={}, productId={}, qty={}, unitPrice={}",
                dto.getCustomerId(), dto.getProductId(), dto.getQuantity(), dto.getUnitPrice());

        // VALIDATION
        if (dto.getQuantity() < 0) {
            log.warn("Invalid quantity provided | quantity={}", dto.getQuantity());
            throw new CartException(COConstants.MSG_INVALID_QUANTITY);
        }

        // CHECK EXISTING CART
        CoCustomerCart cart = cartRepository
                .findByCustomerIdAndProductId(dto.getCustomerId(), dto.getProductId())
                .orElse(null);

        log.debug("Existing cart item found | exists={}", cart != null);

        //  REMOVE ITEM
        if (dto.getQuantity() == 0) {

            if (cart != null) {
                cartRepository.delete(cart);
                log.info("Cart item removed | customerId={}, productId={}", dto.getCustomerId(), dto.getProductId());
            } else {
                log.warn("Attempted to remove non-existent cart item | customerId={}, productId={}",
                        dto.getCustomerId(), dto.getProductId());
            }

            return COConstants.MSG_CART_REMOVED;
        }

        //  CALCULATE TOTAL
        BigDecimal totalPrice = dto.getUnitPrice()
                .multiply(BigDecimal.valueOf(dto.getQuantity()));

        log.debug("Calculated total price | unitPrice={}, quantity={}, totalPrice={}",
                dto.getUnitPrice(), dto.getQuantity(), totalPrice);

        //  UPDATE EXISTING
        if (cart != null) {

            cart.setQuantity(dto.getQuantity());
            cart.setTotalPrice(totalPrice);
            cart.setUpdatedAt(LocalDateTime.now());
            cart.setUpdatedBy(1);

            cartRepository.save(cart);

            log.info("Cart UPDATED | cartId={}, qty={}, totalPrice={}",
                    cart.getCartId(), dto.getQuantity(), totalPrice);

            return COConstants.MSG_CART_UPDATED;
        }

        //  CREATE NEW
        CoCustomerCart newCart = new CoCustomerCart();

        newCart.setCustomerId(dto.getCustomerId());
        newCart.setProductId(dto.getProductId());
        newCart.setQuantity(dto.getQuantity());
        newCart.setTotalPrice(totalPrice);
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setCreatedBy(1);

        cartRepository.save(newCart);

        log.info("Cart CREATED | customerId={}, productId={}, qty={}, totalPrice={}",
                dto.getCustomerId(), dto.getProductId(), dto.getQuantity(), totalPrice);

        return COConstants.MSG_CART_ADDED;
    }
}