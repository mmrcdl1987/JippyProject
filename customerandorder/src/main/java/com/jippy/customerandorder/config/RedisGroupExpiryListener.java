package com.jippy.customerandorder.config;

import com.jippy.customerandorder.repository.GroupOrderInvitationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class RedisGroupExpiryListener implements MessageListener {

    @Autowired
    private GroupOrderInvitationRepository groupOrderInvitationRepository;

    @Override
    @Transactional // Required because we are writing an UPDATE statement to PostgreSQL
    public void onMessage(Message message, byte[] pattern) {
        // 1. Get the expired key string (e.g., "group:expiry:142")
        String expiredKey = message.toString();

        // 2. Filter out other keys so we only process group order expirations
        if (expiredKey.startsWith("group:expiry:")) {

            // 3. Extract the primary key ID from the string string split
            String idString = expiredKey.replace("group:expiry:", "");
            Integer groupInvitationId = Integer.parseInt(idString);

            log.info("Redis Event: Session {} hit 30 minutes. Expiring in DB now...", groupInvitationId);

            // 4. Update your PostgreSQL database status via your standard repository
            groupOrderInvitationRepository.findById(groupInvitationId).ifPresent(invitation -> {
                if ("ACTIVE".equals(invitation.getStatus())) {
                    invitation.setStatus("EXPIRED");
                    invitation.setUpdatedAt(LocalDateTime.now());
                    groupOrderInvitationRepository.save(invitation);

                    // PRO-TIP: You can trigger your Kafka notification event here
                    // to notify the customer mobile UIs to update instantly!
                }
            });
        }
        //Community Group Order Activation ---
        if (expiredKey.startsWith("event:activate:invitation:")) {
            Integer invitationId = Integer.parseInt(expiredKey.replace("event:activate:invitation:", ""));
            log.info("Flipping Group Order Invitation ID {} status to ACTIVE", invitationId);

            groupOrderInvitationRepository.updateStatusToActive(invitationId);
        }
    }
}
