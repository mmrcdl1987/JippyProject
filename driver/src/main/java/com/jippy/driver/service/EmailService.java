package com.jippy.driver.service;

import java.io.File;

/**
 * Common Email Service interface.
 *
 * Business layer should only depend on this interface.
 * Actual implementation can be Gmail, AWS SES, SendGrid etc.
 */
public interface EmailService {


    void sendDriverRegistrationEmail(
            String driverEmail,
            String driverName
    );

    void sendDriverApprovedEmail(
            String driverEmail,
            String driverName
    );

}
