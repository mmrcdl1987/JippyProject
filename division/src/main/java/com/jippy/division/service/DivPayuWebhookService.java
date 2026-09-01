package com.jippy.division.service;

import java.util.Map;

public interface DivPayuWebhookService {

    boolean processPayUWebhook(Map<String, String> payuParams);

    boolean processRefundWebhook(Map<String, String> params);
}
