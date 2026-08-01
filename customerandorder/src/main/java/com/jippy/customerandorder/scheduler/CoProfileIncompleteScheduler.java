package com.jippy.customerandorder.scheduler;

import com.jippy.customerandorder.dto.CoProfileIncompleteCustomer;
import com.jippy.customerandorder.iservice.ICoCustomerService;
import com.jippy.customerandorder.producer.CoProfileIncompleteKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoProfileIncompleteScheduler {

    private final ICoCustomerService customerService;
    private final CoProfileIncompleteKafkaProducer kafkaProducer;

    @Scheduled(fixedRate = 60000) // every 1 minute for testing
    public void sendProfileIncompleteNotifications() {

        log.info("PROFILE_INCOMPLETE_SCHEDULER_STARTED");

        List<CoProfileIncompleteCustomer> customers =
                customerService.getProfileIncompleteCustomers();

        if (customers.isEmpty()) {
            log.info("No profile incomplete customers found.");
            return;
        }

        customers.forEach(kafkaProducer::sendNotification);

        log.info("Published {} customers to Kafka.", customers.size());
    }
}
