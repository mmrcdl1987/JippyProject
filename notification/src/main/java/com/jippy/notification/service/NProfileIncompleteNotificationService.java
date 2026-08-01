package com.jippy.notification.service;

import com.jippy.notification.dto.CoProfileIncompleteCustomer;

public interface NProfileIncompleteNotificationService {

    void processNotification(CoProfileIncompleteCustomer event);

}