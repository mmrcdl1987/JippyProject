//package com.jippy.customerandorder.scheduler;
//
//import com.jippy.customerandorder.entity.CoOrderWaitingPeriod;
//import com.jippy.customerandorder.repository.CoOrderWaitingPeriodRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class CoFinalRejectScheduler {
//
//    private final CoOrderWaitingPeriodRepository
//            repository;
//
//    @Scheduled(fixedRate = 60000)
//    public void enableFinalRejection() {
//
//        log.info(
//                "Final rejection scheduler started");
//
//        List<CoOrderWaitingPeriod> waitingPeriods =
//                repository.fetchPendingWaitingPeriods(
//                        LocalDateTime.now()
//                                .minusMinutes(2));
//
//        for (CoOrderWaitingPeriod waiting
//                : waitingPeriods) {
//
//            waiting.setAllowsRejection(true);
//
//            waiting.setUpdatedAt(
//                    LocalDateTime.now());
//
//            repository.save(waiting);
//
//            log.info(
//                    "Final rejection enabled for waitingPeriodId : {}",
//                    waiting.getOrderWaitingPeriodId());
//        }
//
//        log.info(
//                "Final rejection scheduler completed");
//    }
//}