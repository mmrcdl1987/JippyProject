package com.jippy.notification.mapper;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NWalletPointsEvent;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.OrderNotificationStatus;

import java.time.LocalDateTime;

public class NWalletNotificationMapper {

    private NWalletNotificationMapper() {
    }

    /**
     * Creates notification status record
     * before sending the Firebase notification.
     */
    public static OrderNotificationStatus toNotificationStatus(NWalletPointsEvent event,
                                                               Notification notification,
                                                               NDeviceToken deviceToken) {

        OrderNotificationStatus status = new OrderNotificationStatus();

        status.setOrderId(event.getOrderId());

        status.setNotificationId(notification.getNotificationId());

        status.setNotificationRecipientId(event.getCustomerId());

        status.setRecipientType(NConstants.ROLE_CUSTOMER);

        status.setDeviceTokenId(deviceToken.getDeviceTokenId());

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

        return notification.getMessage().
                replace("{points}",
                        String.valueOf(event.getTransactionPoints()));
    }
}