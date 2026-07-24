package com.jippy.customerandorder.scheduler;

import com.jippy.customerandorder.repository.GroupOrderInvitationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupRunner {

    private final GroupOrderInvitationRepository groupOrdersInvitationRepository;

    /**
     * This is a fallback to turn any created group orders to active state if redis is down,
     * This method runs automatically right after the Spring Boot container
     * is fully initialized and ready to accept requests.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationStartup() {
        log.info("App startup: Checking for pending community group orders to activate...");

        try {
            int updatedCount = groupOrdersInvitationRepository.activatePendingCommunityGroupOrders();
            log.info("App startup sync completed: Activated {} community group order invitations.", updatedCount);
        } catch (Exception e) {
            log.error("Failed to execute startup activation query", e);
        }
    }
}
