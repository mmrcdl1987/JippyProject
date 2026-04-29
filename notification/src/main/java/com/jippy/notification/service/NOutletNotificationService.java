
package com.jippy.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jippy.notification.dto.NOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NOutletNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NOutletNotificationService.class);

    @KafkaListener(topics = "new-orders", groupId = "outlet-group")
    public void sendNewOrderNotification(NOrderEvent orderEvent) {

        String outletTopic = "outlet_" + orderEvent.getOutletId();
        Message message = Message.builder()
                .setTopic(outletTopic)
                .setNotification(Notification.builder()
                        .setTitle("New Order Received!")
                        .setBody("Order #" + orderEvent.getOrderId() + " is waiting for your confirmation")
                        .build())
                .putData("orderId", String.valueOf(orderEvent.getOrderId())) // Data payload for the app to process
                // .putData("click_action", "OPEN_ORDER_DETAIL")
                .build();
        // send notification to notification receipt table

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent notification to outlet: {} placed by customer: {} for order of orderId: {}",
                    orderEvent.getOutletId(), orderEvent.getCustomerId(), orderEvent.getOrderId());
        } catch (FirebaseMessagingException e)  {
        //catch (Exception e)  {
            logger.info("Exception occured in sending notification to outlet: {} placed by customer: {} for order of orderId: {}",
                    orderEvent.getOutletId(), orderEvent.getCustomerId(), orderEvent.getOrderId());
            logger.error(e.getMessage());
        }
    }
}

