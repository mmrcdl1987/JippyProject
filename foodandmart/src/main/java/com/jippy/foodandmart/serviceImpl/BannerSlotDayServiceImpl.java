package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;
import com.jippy.foodandmart.entity.BannerSlotDay;
import com.jippy.foodandmart.mapper.BannerSlotDayMapper;
import com.jippy.foodandmart.repository.BannerSlotDayRepository;
import com.jippy.foodandmart.service.BannerSlotDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BannerSlotDayServiceImpl implements BannerSlotDayService {

    private final BannerSlotDayRepository repository;
    private final BannerSlotDayMapper mapper;
    private static final int INITIAL_MONTHS = 4;
    private static final int EXTEND_MONTHS = 2;
    private static final int SLOT_DAYS = 5;

    private static final int SETTLEMENT_MONTHS = 12;

    private static final String BANNER_SLOT = "banner_slot";
    private static final String SETTLEMENT_SLOT = "settlement_week";

    @Override
    public void generateInitialFourMonths() {

        if (repository.existsBySlotType(BANNER_SLOT)) {

            log.info("Banner slots already generated.");

            return;
        }

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);

        LocalDate endOfFourthMonth = startDate
                .plusMonths(INITIAL_MONTHS)
                .minusDays(1);

        generateInitialSlots(startDate, endOfFourthMonth);

        log.info("Initial {} months banner slots generated successfully.",
                INITIAL_MONTHS);
    }
    @Override
    public void maintainBannerSlots() {

        Optional<BannerSlotDay> optional =
                repository.findTopBySlotTypeOrderBySlotEndDateDesc(BANNER_SLOT);

        if (optional.isEmpty()) {

            log.info("No banner slots found.");

            generateInitialFourMonths();

            return;
        }

        BannerSlotDay lastSlot = optional.get();

        LocalDate today = LocalDate.now();

        LocalDate thresholdDate = today.plusMonths(EXTEND_MONTHS);

        log.info("Today : {}", today);
        log.info("Banner slots available till : {}", lastSlot.getSlotEndDate());

        if (lastSlot.getSlotEndDate().isAfter(thresholdDate)) {

            log.info("Banner slots are already available till {}",
                    lastSlot.getSlotEndDate());

            return;
        }

        LocalDate nextStartDate = lastSlot.getSlotEndDate().plusDays(1);

        LocalDate targetDate = nextStartDate
                .plusMonths(EXTEND_MONTHS)
                .minusDays(1);

        appendSlots(nextStartDate, targetDate);

        log.info("Next {} months banner slots generated successfully.",
                EXTEND_MONTHS);
    }

    @Override
    public void generateInitialSettlementWeeks() {

        if (repository.existsBySlotType(SETTLEMENT_SLOT)) {
            log.info("Settlement week slots already generated.");
            return;
        }

        LocalDate startDate = LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        LocalDate targetDate = startDate
                .plusMonths(SETTLEMENT_MONTHS)
                .minusDays(1);

        generateSettlementSlots(startDate, targetDate);

        log.info("Initial {} months settlement week slots generated successfully.",
                SETTLEMENT_MONTHS);
    }

    @Override
    public void maintainSettlementWeeks() {

        Optional<BannerSlotDay> optional =
                repository.findTopBySlotTypeOrderBySlotEndDateDesc(SETTLEMENT_SLOT);

        if (optional.isEmpty()) {

            log.info("No settlement week slots found.");

            generateInitialSettlementWeeks();

            return;
        }

        BannerSlotDay lastSlot = optional.get();

        LocalDate today = LocalDate.now();

        LocalDate thresholdDate = today.plusMonths(2);

        if (lastSlot.getSlotEndDate().isAfter(thresholdDate)) {

            log.info("Settlement week slots are already available till {}",
                    lastSlot.getSlotEndDate());

            return;
        }

        LocalDate nextStartDate = lastSlot.getSlotEndDate().plusDays(1);

        LocalDate targetDate = nextStartDate
                .plusMonths(EXTEND_MONTHS)
                .minusDays(1);

        appendSettlementSlots(nextStartDate, targetDate);

        log.info("Next {} months settlement week slots generated successfully.",
                EXTEND_MONTHS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerSlotDayResponseDto> getAllSlots() {

        List<BannerSlotDay> entities =
                repository.findAll(Sort.by("slotStartDate"));

        List<BannerSlotDayResponseDto> response =
                new ArrayList<>();

        for (BannerSlotDay entity : entities) {
            response.add(mapper.toResponseDto(entity));
        }

        return response;
    }
    private void generateSettlementSlots(LocalDate startDate,
                                         LocalDate targetDate) {

        List<BannerSlotDay> slots = new ArrayList<>();

        while (!startDate.isAfter(targetDate)) {

            BannerSlotDay slot = createSettlementSlot(startDate);

            slots.add(slot);

            startDate = startDate.plusWeeks(1);
        }

        repository.saveAll(slots);

        log.info("{} settlement week slots generated successfully.",
                slots.size());
    }
    private void appendSettlementSlots(LocalDate startDate,
                                       LocalDate targetDate) {

        List<BannerSlotDay> slots = new ArrayList<>();

        while (!startDate.isAfter(targetDate)) {

            BannerSlotDay slot = createSettlementSlot(startDate);

            slots.add(slot);

            startDate = startDate.plusWeeks(1);
        }

        repository.saveAll(slots);

        log.info("{} settlement week slots appended successfully.",
                slots.size());
    }
    private BannerSlotDay createSettlementSlot(LocalDate startDate) {

        BannerSlotDay slot = new BannerSlotDay();

        slot.setSlotStartDate(startDate);
        slot.setSlotEndDate(startDate.plusDays(6));

        slot.setSlotType(SETTLEMENT_SLOT);

        slot.setCreatedAt(LocalDateTime.now());
        slot.setCreatedBy(1);

        slot.setUpdatedAt(LocalDateTime.now());
        slot.setUpdatedBy(1);

        return slot;
    }
    private void generateInitialSlots(LocalDate startDate,
                                      LocalDate endDate) {

        List<BannerSlotDay> slots = new ArrayList<>();

        while (!startDate.isAfter(endDate)) {

            BannerSlotDay slot = createSlot(startDate);

            slots.add(slot);

            startDate = slot.getSlotEndDate().plusDays(1);
        }

        repository.saveAll(slots);

        log.info("Initial {} slots generated successfully.", slots.size());
    }

    private void appendSlots(LocalDate startDate,
                             LocalDate targetDate) {

        List<BannerSlotDay> slots = new ArrayList<>();

        while (!startDate.isAfter(targetDate)) {

            BannerSlotDay slot = createSlot(startDate);

            slots.add(slot);

            startDate = slot.getSlotEndDate().plusDays(1);
        }

        repository.saveAll(slots);

        log.info("{} new slots appended successfully.", slots.size());
    }

    private BannerSlotDay createSlot(LocalDate startDate) {

        BannerSlotDay slot = new BannerSlotDay();

        slot.setSlotStartDate(startDate);
        slot.setSlotEndDate(startDate.plusDays(SLOT_DAYS - 1));

        slot.setSlotType(BANNER_SLOT);

        slot.setCreatedAt(LocalDateTime.now());
        slot.setCreatedBy(1);

        slot.setUpdatedAt(LocalDateTime.now());
        slot.setUpdatedBy(1);

        return slot;
    }
}