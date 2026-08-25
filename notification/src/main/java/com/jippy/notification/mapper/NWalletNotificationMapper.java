package com.jippy.notification.mapper;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NWalletPointsEvent;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.WalletNotificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NWalletNotificationMapper {

    private NWalletNotificationMapper() {
    }

    /**
     * Creates notification status record
     * before sending the Firebase notification.
     */
    public static WalletNotificationStatus toNotificationStatus(NWalletPointsEvent event,
                                                                Notification notification,
                                                                NDeviceToken deviceToken) {

        WalletNotificationStatus status = new WalletNotificationStatus();

        status.setOrderId(event.getOrderId());

        status.setNotificationId(notification.getNotificationId());

        status.setNotificationRecipientId(event.getCustomerId());

        status.setRecipientType(NConstants.ROLE_CUSTOMER);

        status.setDeviceTokenId(deviceToken.getDeviceTokenId());

        status.setReferenceId(event.getCustomerId());

        status.setReferenceType(event.getPointsType() != null ? event.getPointsType() : "WALLET_POINTS");

        status.setNotificationStatus(false);

        status.setSentAt(LocalDateTime.now());

        status.setCreatedAt(LocalDateTime.now());

//        status.setCreatedBy(1);

        return status;
    }


    /**
     * Replaces notification template placeholders
     * with actual wallet points.
     */
    public static String buildWalletPointsMessage(Notification notification, NWalletPointsEvent event) {

        String message = notification.getMessage()
                .replace("{points}", String.valueOf(event.getTransactionPoints()));

        BigDecimal amount = event.getConvertedAmount();
        if (amount != null) {
            message = message.replace("{amount}", amount.toPlainString());
        }

        return message;
    }
}
