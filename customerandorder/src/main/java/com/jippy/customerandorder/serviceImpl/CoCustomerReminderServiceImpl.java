package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoMealReminderDto;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.iservice.ICustomerReminderService;
import com.jippy.customerandorder.producer.CoMealReminderKafkaProducer;
import com.jippy.customerandorder.repository.CoCustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoCustomerReminderServiceImpl implements ICustomerReminderService {

    private final CoCustomerRepository customerRepository;

    private final CoMealReminderKafkaProducer mealReminderKafkaProducer;

    @Override
    public void processMealReminder(String mealType) {

        log.info("SERVICE_START | PROCESS_MEAL_REMINDER | MealType={}", mealType);

        if (mealType == null || mealType.isBlank()) {

            log.warn("Invalid meal type received. Skipping meal reminder.");

            return;
        }

        List<CoCustomer> customers =
                customerRepository.findAllCustomersForMealReminder();

        if (customers == null || customers.isEmpty()) {

            log.warn("No customers found for meal reminder notification.");

            return;
        }

        log.info("Total Customers Found={}", customers.size());

        int successCount = 0;
        int failedCount = 0;

        for (CoCustomer customer : customers) {

            try {

                /*
                 * Unique Reference Id for one Meal Type per Day.
                 *
                 * Example:
                 * Breakfast + 2026-07-31 -> 123456789
                 * Lunch + 2026-07-31 -> 987654321
                 */
                Integer referenceId =
                        Math.abs((mealType.toUpperCase() + LocalDate.now()).hashCode());

                CoMealReminderDto dto =
                        CoMealReminderDto.builder()
                                .customerId(customer.getCustomerId())
                                .mealType(mealType)
                                .referenceId(referenceId)
                                .build();

                mealReminderKafkaProducer.sendMealReminder(dto);

                successCount++;

            } catch (Exception ex) {

                failedCount++;

                log.error(
                        "Failed to publish Meal Reminder | CustomerId={} | MealType={}",
                        customer.getCustomerId(),
                        mealType,
                        ex
                );
            }
        }

        log.info(
                "SERVICE_END | PROCESS_MEAL_REMINDER | MealType={} | TotalCustomers={} | Success={} | Failed={}",
                mealType,
                customers.size(),
                successCount,
                failedCount
        );
    }
}