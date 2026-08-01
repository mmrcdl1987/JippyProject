package com.jippy.customerandorder.iservice;

public interface ICustomerReminderService {

    /**
     * Publish meal reminder notifications for all customers.
     *
     * @param mealType Current meal type
     */
    void processMealReminder(String mealType);

}