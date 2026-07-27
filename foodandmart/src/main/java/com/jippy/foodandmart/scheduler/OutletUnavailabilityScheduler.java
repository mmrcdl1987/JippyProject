package com.jippy.foodandmart.scheduler;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.entity.OutletUnavailability;
import com.jippy.foodandmart.exception.OutletUnavailabilitySchedulerException;
import com.jippy.foodandmart.repository.FmOutletCategoryRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.repository.OutletUnavailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutletUnavailabilityScheduler {

    private static final int BATCH_SIZE = 1000;

    private final OutletUnavailabilityRepository repository;

    private final FmProductRepository productRepository;

    private final FmOutletRepository outletRepository;

    private final FmOutletCategoryRepository outletCategoryRepository;

    /**
     * Production Cron
     * Every 2 hours
     */
    @Scheduled(cron = "0 0 */2 * * *")

    /**
     * Testing Cron
     * Every 30 seconds
     */
   // @Scheduled(cron = "*/30 * * * * *")
    @Transactional
    public void restoreAvailability() {

        LocalDateTime currentDateTime = LocalDateTime.now();

        log.info("RESTORE_UNAVAILABILITY_SCHEDULER START | threadName={}, currentDateTime={}, batchSize={}", Thread.currentThread().getName(), currentDateTime, BATCH_SIZE);

        int pageNumber = 0;

        int totalProcessedRecords = 0;

        int successCount = 0;

        int failedCount = 0;

        Page<OutletUnavailability> expiredRecords;

        do {

            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);

            try {

                log.debug("FETCH_EXPIRED_RECORDS START | pageNumber={}, batchSize={}", pageNumber, BATCH_SIZE);

                expiredRecords = repository.findExpiredUnavailability(currentDateTime, pageable);

                log.info("FETCH_EXPIRED_RECORDS SUCCESS | pageNumber={}, fetchedCount={}", pageNumber, expiredRecords.getNumberOfElements());

            } catch (Exception ex) {

                log.error("FETCH_EXPIRED_RECORDS FAILED | pageNumber={}", pageNumber, ex);

                throw new OutletUnavailabilitySchedulerException("Unable to fetch expired unavailability records");
            }

            if (expiredRecords.isEmpty()) {

                log.info("No expired records found | pageNumber={}", pageNumber);

                break;
            }

            for (OutletUnavailability record : expiredRecords.getContent()) {

                try {

                    log.debug("PROCESS_EXPIRED_RECORD START | outletUnavailabilityId={}, type={}, unavailabilityId={}, endDateTime={}", record.getOutletUnavailabilityId(), record.getType(), record.getUnavailabilityId(), record.getUnavailabilityToDate());

                    validateRecord(record);

                    restoreToggle(record.getType(), record.getUnavailabilityId());

                    deleteProcessedRecord(record);

                    successCount++;

                    log.info("PROCESS_EXPIRED_RECORD SUCCESS | outletUnavailabilityId={}, type={}, unavailabilityId={}", record.getOutletUnavailabilityId(), record.getType(), record.getUnavailabilityId());

                } catch (Exception ex) {

                    failedCount++;

                    log.error("PROCESS_EXPIRED_RECORD FAILED | outletUnavailabilityId={}, type={}, unavailabilityId={}", record.getOutletUnavailabilityId(), record.getType(), record.getUnavailabilityId(), ex);
                }

                totalProcessedRecords++;
            }

            pageNumber++;

        } while (expiredRecords.hasNext());

        log.info("RESTORE_UNAVAILABILITY_SCHEDULER SUCCESS | totalProcessedRecords={}, successCount={}, failedCount={}", totalProcessedRecords, successCount, failedCount);
    }

    private void validateRecord(OutletUnavailability record) {

        log.debug("VALIDATE_RECORD START | outletUnavailabilityId={}", record.getOutletUnavailabilityId());

        if (record.getType() == null || record.getUnavailabilityId() == null) {

            log.warn("VALIDATE_RECORD FAILED | outletUnavailabilityId={}, type={}, unavailabilityId={}", record.getOutletUnavailabilityId(), record.getType(), record.getUnavailabilityId());

            throw new OutletUnavailabilitySchedulerException("Invalid expired unavailability record");
        }

        log.debug("VALIDATE_RECORD SUCCESS | outletUnavailabilityId={}", record.getOutletUnavailabilityId());
    }

    private void restoreToggle(String type, Integer id) {

        log.debug("RESTORE_TOGGLE START | type={}, id={}", type, id);

        try {

            switch (type.toUpperCase()) {

                case FmAppConstants.PRODUCT -> {

                    productRepository.enableProduct(id);

                    log.info("PRODUCT_ENABLE SUCCESS | productId={}", id);
                }

                case FmAppConstants.OUTLET -> {

                    outletRepository.enableOutlet(id);

                    log.info("OUTLET_ENABLE SUCCESS | outletId={}", id);
                }

                case FmAppConstants.OUTLET_CATEGORY -> {

                    outletCategoryRepository.enableOutletCategory(id);

                    log.info("OUTLET_CATEGORY_ENABLE SUCCESS | outletCategoryId={}", id);
                }

                default -> {

                    log.warn("RESTORE_TOGGLE FAILED | Invalid type={}, id={}", type, id);

                    throw new OutletUnavailabilitySchedulerException("Invalid unavailability type");
                }
            }

        } catch (Exception ex) {

            log.error("RESTORE_TOGGLE FAILED | type={}, id={}", type, id, ex);

            throw new OutletUnavailabilitySchedulerException("Unable to restore availability toggle");
        }

        log.debug("RESTORE_TOGGLE SUCCESS | type={}, id={}", type, id);
    }

    private void deleteProcessedRecord(OutletUnavailability record) {

        log.debug("DELETE_PROCESSED_RECORD START | outletUnavailabilityId={}", record.getOutletUnavailabilityId());

        try {

            repository.deleteById(record.getOutletUnavailabilityId());

            log.info("DELETE_PROCESSED_RECORD SUCCESS | outletUnavailabilityId={}", record.getOutletUnavailabilityId());

        } catch (Exception ex) {

            log.error("DELETE_PROCESSED_RECORD FAILED | outletUnavailabilityId={}", record.getOutletUnavailabilityId(), ex);

            throw new OutletUnavailabilitySchedulerException("Unable to delete processed record");
        }

        log.debug("DELETE_PROCESSED_RECORD SUCCESS | outletUnavailabilityId={}", record.getOutletUnavailabilityId());
    }
}