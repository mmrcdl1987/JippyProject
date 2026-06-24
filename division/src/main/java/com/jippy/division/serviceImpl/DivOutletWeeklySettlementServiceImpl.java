package com.jippy.division.serviceImpl;


import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.DivOutletWeeklySettlementResponseDto;
import com.jippy.division.entity.DivOutletWeeklySettlement;
import com.jippy.division.exception.ResourceNotFoundException;
import com.jippy.division.mapper.DivOutletWeeklySettlementMapper;
import com.jippy.division.repositary.DivOutletWeeklySettlementRepository;
import com.jippy.division.service.DivOutletWeeklySettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DivOutletWeeklySettlementServiceImpl implements DivOutletWeeklySettlementService {

    private final DivOutletWeeklySettlementRepository divOutletWeeklySettlementRepository;

    private final DivOutletWeeklySettlementMapper divOutletWeeklySettlementMapper;

    private final EmailService emailService;

    @Override
    public DivOutletWeeklySettlementResponseDto getOutletWeeklySettlement(Integer weeklySettlementId) {

        log.info("Fetching outlet weekly settlement details for weeklySettlementId : {}", weeklySettlementId);

        DivOutletWeeklySettlement settlement = divOutletWeeklySettlementRepository.findById(weeklySettlementId).orElseThrow(() -> {

            log.error("Outlet weekly settlement not found with weeklySettlementId : {}", weeklySettlementId);

            return new ResourceNotFoundException("Outlet weekly settlement not found with weeklySettlementId : " + weeklySettlementId);
        });

        log.info("Successfully fetched outlet weekly settlement details for weeklySettlementId : {}", weeklySettlementId);

        return divOutletWeeklySettlementMapper.convertToResponseDto(settlement);
    }

//    @Override
//    public void sendOutletSettlementMail(Integer weeklySettlementId) {
//        System.out.println("schedular for every 1 min");
//        DivOutletWeeklySettlement settlement =
//                divOutletWeeklySettlementRepository.
//                        findById(weeklySettlementId).orElseThrow(
//                                () -> new ResourceNotFoundException("Settlement Not Found"));
//
//        emailService.sendSettlementMail(settlement);
//       }
//        ----------------------------------------------------------------


//     Instead of controller [Schedular] is calling this service
    @Override
    public void sendOutletSettlementMail() {

        List<DivOutletWeeklySettlement> settlements =
                divOutletWeeklySettlementRepository.
                        findByPaymentStatusAndEmailStatus
                    (DivAppConstants.PAYMENT_STATUS_PAID,DivAppConstants.EMAIL_STATUS_PENDING);

        if (settlements.isEmpty()) {
            log.info("No eligible settlements found");
            return;
        }

        for (DivOutletWeeklySettlement settlement : settlements) {

            try {

//                 sending mail / calling mail service
                emailService.sendSettlementMail(settlement);

//                settlement.setEmailStatus("EMAIL_SENT");
                settlement.setEmailStatus(DivAppConstants.EMAIL_STATUS_SENT);

            divOutletWeeklySettlementRepository.save(settlement);

            } catch (Exception e) {
                log.error("Failed to send mail for settlementId : {}",
                        settlement.getWeeklySettlementId(), e);
            }
        }
    }


//    @Scheduled(cron = "0 * * * * *")
//    public void scheduleSettlementMail() {
//        System.out.println("schedular for every minute");
//        sendOutletSettlementMail(1);
//
// }
//
}