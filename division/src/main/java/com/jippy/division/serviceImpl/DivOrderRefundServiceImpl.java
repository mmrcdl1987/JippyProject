package com.jippy.division.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.DivOrderDto;
import com.jippy.division.dto.DivPaymentModesDto;
import com.jippy.division.entity.OrderRefund;
import com.jippy.division.entity.PaymentTransaction;
import com.jippy.division.feignClient.CoFeignClient;
import com.jippy.division.mapper.DivPaymentMapper;
import com.jippy.division.repositary.OrderRefundRepository;
import com.jippy.division.repositary.TransactionRepository;
import com.jippy.division.service.DivOrderRefundService;
import com.jippy.division.service.PayUService;
import com.paytm.pg.merchant.PaytmChecksum;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DivOrderRefundServiceImpl implements DivOrderRefundService {

    private final RazorpayClient razorpayClient;
    private final TransactionRepository transactionRepository;
    private final OrderRefundRepository refundRepository;
    private final CoFeignClient coFeignClient;
    private final RestTemplate restTemplate = new RestTemplate();
    private final PayUService payUService;
    private final DivPaymentMapper paymentMapper;

    @Value("${paytm.mid}")
    private String paytmMid;

    @Value("${paytm.merchantKey}")
    private String paytmMerchantKey;

    @Value("${paytm.initiate-txn-url}")
    private String paytmUrl;

    @Value("${paytm.refund-url}")
    private String paytmRefundUrl;

    @Override
    public String orderRefund(String orderId, String reason)  {
        log.info("Initiating refund for orderId: {}, reason: {}", orderId, reason);

        // 1. Fetch original Order and completed Transaction
        DivOrderDto orderDto =  coFeignClient.getOrder(orderId);
        if(orderDto == null) {
            throw new RuntimeException("Order not found for refund: " + orderId);
        }

        PaymentTransaction tx = transactionRepository.findByApplicationOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Transaction record not found"));

        if (!"SUCCESS".equals(tx.getPaymentStatus())) {
            throw new IllegalStateException("Cannot refund an un-captured or failed transaction.");
        }

        ResponseEntity<DivPaymentModesDto> paymentModesDtoResponseEntity =
                coFeignClient.getPaymentModeById(tx.getPaymentMethodType());

        DivPaymentModesDto paymentModesDto = paymentModesDtoResponseEntity.getBody();
        if(paymentModesDto == null) {
            throw new RuntimeException("Payment mode not found for ID: " + tx.getPaymentMethodType());
        }

        String response = "";
        if(paymentModesDto.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_RAZOR_PAY)){
            response= initiateRazorPayRefund(orderId,tx,reason,orderDto);
            return response;
        }
        if(paymentModesDto.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_PAYTM)){
            response = initiatePaytmRefund(orderId,tx,reason,orderDto);
            return response;
        }

        if(paymentModesDto.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_PAYU)){
            response = initiatePayURefund(orderId,tx,reason,orderDto);
            return response;
        }
        return "Refund request failed for orderId: " + orderId + ". Please check payment gateway dashboard for details.";
    }

    private String initiatePayURefund(String orderId, PaymentTransaction tx, String reason, DivOrderDto orderDto) {

        // 1. Generate unique Refund Request ID
        UUID refundTxnId = UUID.randomUUID();

        // 2. Call PayU Refund API (var1 must be valid PayU gateway payment ID)
        boolean refundStatus = payUService.initiateRefund(
                tx.getGatewayPaymentId(),
                refundTxnId.toString(),
                tx.getAmount()
        );

        // 3. Update status on main payment transaction
        String newStatus = refundStatus ? DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED
                : DivAppConstants.PAYMENT_STATUS_REFUND_FAILED;

        LocalDateTime now = LocalDateTime.now();

        if (refundStatus) {
            tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
            tx.setUpdatedAt(now);
            transactionRepository.save(tx);
            log.info("PayU Refund successfully initiated for OrderId: {} | RefundTxnID: {}", orderId, refundTxnId);
        } else {
            log.error("PayU Refund failed to initiate for OrderId: {} | RefundTxnID: {}", orderId, refundTxnId);
        }

        // 4. Map and persist refund record ONLY ONCE at the end
        OrderRefund orderRefund = paymentMapper.mapToRefundOrder(orderId, tx, null, reason, refundTxnId);
        orderRefund.setRefundStatus(newStatus);
        orderRefund.setCreatedAt(now);
        orderRefund.setUpdatedAt(now);

        refundRepository.save(orderRefund);

        return (refundStatus ? "PayU Refund successfully initiated" : "PayU Refund failed to initiate")
                + " for OrderId: " + orderId + " | RefundTxnID: " + refundTxnId;
    }

    @Transactional
    private String initiateRazorPayRefund(String orderId, PaymentTransaction tx, String reason, DivOrderDto orderDto) {
        // 2. Build the Razorpay Refund Payload
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("payment_id", tx.getGatewayPaymentId()); // The original payment ID
        refundRequest.put("amount", tx.getAmount());     // Full refund (or pass less for partial)

        JSONObject notes = new JSONObject();
        notes.put("order_id",orderId);
        notes.put("reason", reason);
        refundRequest.put("notes", notes);

        try{
            // 3. Execute call against Razorpay
            Refund rzpRefund = razorpayClient.refunds.create(refundRequest);
            String rzpRefundId = rzpRefund.get("id");

            // 4. Update local state
            orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
            coFeignClient.updateOrderStatus(orderDto);

      /*  tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
        transactionRepository.save(tx);*/

            UUID refundTxId = UUID.randomUUID();

            // 5. Audit the refund ledger
            OrderRefund orderRefund = DivPaymentMapper.mapToRefundOrder(orderDto.getOrderId(), tx, rzpRefundId, reason,refundTxId);

            refundRepository.save(orderRefund);

            log.info("Refund initiated successfully for orderId: {},  Razorpay Refund ID: {}", orderId, rzpRefundId);

            return "Refund initiated successfully for orderId: " + orderId + ", Razorpay Refund ID: " + rzpRefundId;
            // Note: Emit a Kafka event here (e.g., "order-refunded")
            // to instantly text the user or notify the driver app to halt.

        }catch (Exception e){
            throw new RuntimeException("Exception occurred in initiating razor pay refund: "+e.getMessage());
        }


    }


    @Transactional
    private String initiatePaytmRefund(String orderId, PaymentTransaction tx, String reason, DivOrderDto orderDto) {

        UUID refundTxId = UUID.randomUUID();
        // 1. Generate a unique refund reference ID for your internal tracking
        OrderRefund orderRefund = DivPaymentMapper.mapToRefundOrder(orderId, tx, null, reason,refundTxId);

        // Convert paise back to standard rupees formatting matching Paytm requirement (e.g. "100.50")
        double decimalAmount = tx.getAmount() / 100.0;
        String formattedAmount = String.format("%.2f", decimalAmount);

        // 2. Build Request Body Map
        Map<String, Object> body = new HashMap<>();
        body.put("mid", paytmMid);
        body.put("orderId", orderId);
        body.put("txnId", tx.getGatewayPaymentId());
        body.put("refId", orderRefund.getRefundTransactionsId().toString());
        body.put("refundAmount", formattedAmount);
        body.put("refundReason", reason);

        try {
            // 3. Generate Security Checksum
            String jsonBody = new ObjectMapper().writeValueAsString(body);
            String checksum = PaytmChecksum.generateSignature(jsonBody, paytmMerchantKey);

            // 4. Set Request Headers & Package Request Envelope
            Map<String, String> head = new HashMap<>();
            head.put("signature", checksum);

            Map<String, Object> paytmRefundPayload = new HashMap<>();
            paytmRefundPayload.put("body", body);
            paytmRefundPayload.put("head", head);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paytmRefundPayload, headers);

            // 5. Post payload directly to Paytm Gateway
            ResponseEntity<Map> response = restTemplate.postForEntity(paytmRefundUrl, entity, Map.class);
            Map<String, Object> responseMap = response.getBody();

            Map<String, Object> resultBody = (Map<String, Object>) responseMap.get("body");
            Map<String, Object> resultInfo = (Map<String, Object>) resultBody.get("resultInfo");

            String resultStatus = (String) resultInfo.get("resultStatus"); // "PENDING" or "TXN_SUCCESS" or "TXN_FAILURE"

            // 6. Persist status adjustments inside your tracking layers
            if ("TXN_SUCCESS".equals(resultStatus) || "PENDING".equals(resultStatus)) {

                // Extract the gateway refund tracking ID returned by Paytm
                String paytmRefundId = (String) resultBody.get("refundId");

                // A. Update the OrderRefund table details
                if ("TXN_SUCCESS".equals(resultStatus)) {
                    orderRefund.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    // tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                } else {
                    orderRefund.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED); // Bank processing is asynchronous
                    // tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
                    orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
                }

                orderRefund.setGatewayRefundId(paytmRefundId); // Storing Paytm's refundId in your generalized column
                refundRepository.save(orderRefund);

                // transactionRepository.save(tx);

                coFeignClient.updateOrderStatus(orderDto);

                log.info("Refund request processed successfully for orderId: {}, Paytm Refund ID: {}, Status: {}", orderId, paytmRefundId, orderRefund.getRefundStatus());

                return "Refund request processed successfully for orderId: " + orderId + ", Paytm Refund ID: " + orderRefund.getGatewayRefundId() + ", Status: " + orderRefund.getRefundStatus();
            }
        } catch (Exception e) {
            orderRefund.setRefundStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);
            //tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);
            orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);

            coFeignClient.updateOrderStatus(orderDto);
            //transactionRepository.save(tx);
            refundRepository.save(orderRefund);
            throw new RuntimeException("CRITICAL: Failed to dispatch Paytm refund execution pipeline", e);
        }
        return "Refund request failed for orderId: " + orderId + ". Please check Paytm dashboard for details.";
    }


}
