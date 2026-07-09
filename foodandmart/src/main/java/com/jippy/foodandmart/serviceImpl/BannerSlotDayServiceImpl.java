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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BannerSlotDayServiceImpl implements BannerSlotDayService {

    private static final int INITIAL_MONTHS = 4;
    private static final int EXTEND_MONTHS = 2;
    private static final int SLOT_DAYS = 5;
    private final BannerSlotDayRepository repository;
    private final BannerSlotDayMapper mapper;

    @Override
    public void generateInitialFourMonths() {

        if (repository.count() > 0) {
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
                repository.findTopByOrderBySlotEndDateDesc();

        if (optional.isEmpty()) {

            log.info("No banner slots found.");

            generateInitialFourMonths();

            return;
        }

        BannerSlotDay lastSlot = optional.get();

        LocalDate currentMonth =
                LocalDate.now().withDayOfMonth(1);

        LocalDate lastGeneratedMonth =
                lastSlot.getSlotStartDate().withDayOfMonth(1);

        long completedMonths =
                ChronoUnit.MONTHS.between(
                        lastGeneratedMonth,
                        currentMonth);

        log.info("Current Month : {}", currentMonth);
        log.info("Last Generated Month : {}", lastGeneratedMonth);
        log.info("Completed Months : {}", completedMonths);

        if (completedMonths < EXTEND_MONTHS) {

            log.info("Two months are not completed yet.");

            return;
        }

        LocalDate nextStartDate =
                lastSlot.getSlotEndDate().plusDays(1);

        LocalDate targetDate =
                nextStartDate
                        .plusMonths(EXTEND_MONTHS)
                        .minusDays(1);

        appendSlots(nextStartDate, targetDate);

        log.info("Next {} months banner slots generated successfully.",
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

        slot.setCreatedAt(LocalDateTime.now());
        slot.setCreatedBy(1);

        slot.setUpdatedAt(LocalDateTime.now());
        slot.setUpdatedBy(1);

        return slot;
    }
}