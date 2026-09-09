package com.jippy.division.scheduler;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.DivOrderDto;
import com.jippy.division.entity.OrderRefund;
import com.jippy.division.entity.PaymentTransaction;
import com.jippy.division.feignClient.CoFeignClient;
import com.jippy.division.repositary.OrderRefundRepository;
import com.jippy.division.repositary.TransactionRepository;
import com.paytm.pg.merchant.PaytmChecksum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaytmRefundStatusScheduler {

    private final OrderRefundRepository orderRefundRepository;
    private final TransactionRepository transactionRepository;
    private final CoFeignClient coFeignClient;

    @Value("${paytm.mid}") private String paytmMid;
    @Value("${paytm.merchant-key}") private String paytmMerchantKey;
    @Value("${paytm.refund-status-url}") private String refundStatusUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Runs every 4 hours automatically to reconcile stuck or pending states
    // @Scheduled(cron = "0 0 */4 * * *")
    @Transactional
    public void checkPendingPaytmRefunds() {
        log.info("Paytm pending refunds schedular initiated");

        // 1. Pull all records matching your pending tracking state
        List<OrderRefund> pendingRefunds = orderRefundRepository.
                findByStatusAndTransactionPaymentMethodType("PENDING", "PAYTM");

        for (OrderRefund refund : pendingRefunds) {
            try {

                String refId = refund.getRefundTransactionsId().toString();

                // 2. Build the Refund Status Request Payload
                Map<String, Object> body = new HashMap<>();
                body.put("mid", paytmMid);
                body.put("orderId", refund.getApplicationOrderId());
                body.put("refId", refId);

                String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
                String checksum = PaytmChecksum.generateSignature(jsonBody, paytmMerchantKey);

                Map<String, String> head = new HashMap<>();
                head.put("signature", checksum);

                Map<String, Object> requestEnvelope = new HashMap<>();
                requestEnvelope.put("body", body);
                requestEnvelope.put("head", head);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestEnvelope, headers);

                // 3. Fire Server-to-Server Request
                ResponseEntity<Map> response = restTemplate.postForEntity(refundStatusUrl, entity, Map.class);
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> resultBody = (Map<String, Object>) responseBody.get("body");
                Map<String, Object> resultInfo = (Map<String, Object>) resultBody.get("resultInfo");

                String resultStatus = (String) resultInfo.get("resultStatus");

                PaymentTransaction paymentTransaction = refund.getPaymentTransaction();

                // 4. Update internal records upon successful completion matching
                if ("TXN_SUCCESS".equals(resultStatus)) {
                    refund.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);

                    // Paytm returns the official bank network transaction tracking id here
                    String bankTxnId = (String) resultBody.get("bankTxnId");
                    refund.setBankArn(bankTxnId); // Storing it directly into your bankArn column!
                    orderRefundRepository.save(refund);

                    paymentTransaction.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    transactionRepository.save(paymentTransaction);

                    DivOrderDto orderDto =  coFeignClient.getOrder(refund.getApplicationOrderId());
                    orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    coFeignClient.updateOrderStatus(orderDto);

                    log.info("Refund is processed successfully for refund transaction id: {} ",refund.getRefundTransactionsId());
                } else if ("TXN_FAILURE".equals(resultStatus)) {
                    refund.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);
                    orderRefundRepository.save(refund);

                    paymentTransaction.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    transactionRepository.save(paymentTransaction);

                    DivOrderDto orderDto =  coFeignClient.getOrder(refund.getApplicationOrderId());
                    orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    coFeignClient.updateOrderStatus(orderDto);

                    log.info("Transaction failed in processing the refund of refund transaction id: {} ",refund.getRefundTransactionsId());
                }

            } catch (Exception e) {
                // Log exception error so individual record failures don't crash the loop
                log.error("Error reconciling payment tracking ID: {}", refund.getRefundTransactionsId());
            }
        }
    }
}

