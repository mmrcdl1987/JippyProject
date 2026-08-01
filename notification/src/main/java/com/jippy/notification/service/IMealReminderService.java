package com.jippy.notification.service;

import com.jippy.notification.dto.NMealReminderDto;

public interface IMealReminderService {

    void processMealReminder(NMealReminderDto reminder);

}