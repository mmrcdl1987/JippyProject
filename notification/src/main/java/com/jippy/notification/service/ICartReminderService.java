package com.jippy.notification.service;

import com.jippy.notification.dto.NCartReminderDto;

public interface ICartReminderService {

    void processReminder(NCartReminderDto reminder);

}