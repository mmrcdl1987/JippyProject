package com.jippy.division.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.*;
import com.jippy.division.entity.OrderRefund;
import com.jippy.division.entity.PaymentTransaction;
import com.jippy.division.mapper.DivPaymentMapper;
import com.jippy.division.repositary.OrderRefundRepository;
import com.jippy.division.repositary.TransactionRepository;
import com.jippy.division.service.DivPaymentService;
import com.paytm.pg.merchant.PaytmChecksum;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DivPaymentServiceImpl implements DivPaymentService {

    @Autowired
    private RazorpayClient razorpayClient;
    private final TransactionRepository transactionRepository;
    private final OrderRefundRepository refundRepository;
    private final CoFeignClient coFeignClient;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${paytm.mid}")
    private String paytmMid;

    @Value("${paytm.merchantKey}")
    private String paytmMerchantKey;

    @Value("${paytm.initiate-txn-url}")
    private String paytmUrl;

    @Value("${paytm.callback-url}")
    private String paytmCallback;

    @Value("${paytm.refund-url}")
    private String paytmRefundUrl;

    @Override
    public DivPaymentInitiateResponse initiatePayment(DivPlaceOrderRequestDto placeOrderRequestDto) throws RazorpayException {

        ResponseEntity<DivPaymentModesDto> paymentModesDtoResponseEntity =
                coFeignClient.getPaymentModeById(placeOrderRequestDto.getPaymentModeId());

        DivPaymentModesDto paymentModesDto = paymentModesDtoResponseEntity.getBody();
        if(paymentModesDto == null) {
            throw new RuntimeException("Payment mode not found for ID: " + placeOrderRequestDto.getPaymentModeId());
        }
        DivPaymentInitiateResponse response = new DivPaymentInitiateResponse();
        if(paymentModesDto.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_RAZOR_PAY)){
            response = initiateRazorPayPayment(placeOrderRequestDto);
            return response;
        }
        if(paymentModesDto.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_PAYTM)){
            response = initiatePaytmPayment(placeOrderRequestDto);
            return response;
        }
        return response;
    }

    private DivPaymentInitiateResponse initiateRazorPayPayment(DivPlaceOrderRequestDto placeOrderRequestDto) throws RazorpayException {

        ResponseEntity<DivOrderDto> placeOrderRequestDtoResponseEntity =
                coFeignClient.placeOrder(placeOrderRequestDto);

        DivOrderDto orderDto = placeOrderRequestDtoResponseEntity.getBody();

        log.info("Initiating razor payment for order: {}", placeOrderRequestDto.getOrderId());

        if(orderDto == null) {
            throw new RuntimeException("Order is not created because of some issue, Please try again " );
        }
        System.out.println("=========================="+orderDto.toString());
        // Convert order total to subunits (Paise)
        int amountInPaise = orderDto.getOrderTotalAmount().multiply(new java.math.BigDecimal(100)).intValue();

        // Construct Razorpay payload
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", orderDto.getOrderId());

        System.out.println("==================call razor pay========");
        // Call Razorpay API
        com.razorpay.Order rzpOrder = razorpayClient.orders.create(orderRequest);
        String rzpOrderId = rzpOrder.get("id");
        System.out.println("==================received razor pay========"+rzpOrderId);

        transactionRepository.findByApplicationOrderIdAndGatewayOrderIdAndPaymentStatus(orderDto.getOrderId(),
                        rzpOrderId, DivAppConstants.PAYMENT_STATUS_PENDING)
                .ifPresent(existingTransaction -> {
                    throw new RuntimeException("Duplicate transaction detected for order: " + orderDto.getOrderId());
                });

        // Save transaction trace as PENDING
        PaymentTransaction transaction = DivPaymentMapper.mapToTransactions(orderDto, rzpOrderId, amountInPaise);
        transactionRepository.save(transaction);

        DivOrderDto divOrderDto =  coFeignClient.getOrder(transaction.getApplicationOrderId());
        divOrderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_PENDING);
        coFeignClient.updateOrderStatus(divOrderDto);

        log.info("Payment initiated successfully for order: {} with Razorpay Order ID: {}", orderDto.getOrderId(), rzpOrderId);

        DivPaymentInitiateResponse response = new DivPaymentInitiateResponse();
        response.setOrderId(orderDto.getOrderId());
        response.setRazorpayOrderId(rzpOrderId);
        response.setToPayAmount(orderDto.getOrderTotalAmount());

        return  response;
    }

    /**
     * Step 3B: Crytographically verify the payment signature received from frontend.
     */
    @Transactional
    public boolean verifyPaymentSignature(PaymentVerifyRequestDto request) {
        try {
            log.info("Verifying payment signature for Razorpay Order ID: {}", request.getRzpOrderId());

            // Generate SHA256 signature locally using the secret
            String generatedPayload = request.getRzpOrderId() + "|" + request.getRzpPaymentId();
            String expectedSignature = Utils.getHash(generatedPayload, keySecret);

            if (expectedSignature.equals(request.getRzpSignature())) {
                // Update records upon matching signature
                PaymentTransaction tx = transactionRepository.findByGatewayOrderId(request.getRzpOrderId())
                        .orElseThrow(() -> new RuntimeException("Transaction lookup failed"));
                tx.setGatewayPaymentId(request.getRzpPaymentId());
                tx.setGatewaySignature(request.getRzpSignature());
                tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_SUCCESS);
                transactionRepository.save(tx);

                DivOrderDto orderDto =  coFeignClient.getOrder(request.getApplicationOrderId());

                if(orderDto != null){
                    orderDto.setOrderStatus(DivAppConstants.START_PREPARING);
                    coFeignClient.updateOrderStatus(orderDto);
                }

                log.info("Payment verified successfully for Razorpay Order ID: {}. Order status updated to START_PREPARING.", request.getRzpOrderId());

                // Note: Emit a Kafka message here (e.g., "payment-success")
                // to trigger downstream notification or driver-assignment microservices.

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Payment signature verification failed for Razorpay Order ID: {}. Error: {}", request.getRzpOrderId(), e.getMessage());
        }
        return false;
    }

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
        return "Refund request failed for orderId: " + orderId + ". Please check payment gateway dashboard for details.";
    }

    @Transactional
    private String initiatePaytmRefund(String orderId, PaymentTransaction tx, String reason, DivOrderDto orderDto) {

        // 1. Generate a unique refund reference ID for your internal tracking
        OrderRefund orderRefund = DivPaymentMapper.mapToRefundOrder(orderDto, tx, null, reason);

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
                    orderRefund.setStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    // tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                    orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_PROCESSED);
                } else {
                    orderRefund.setStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED); // Bank processing is asynchronous
                    // tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
                    orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
                }

                orderRefund.setGatewayRefundId(paytmRefundId); // Storing Paytm's refundId in your generalized column
                refundRepository.save(orderRefund);

                // transactionRepository.save(tx);

                coFeignClient.updateOrderStatus(orderDto);

                log.info("Refund request processed successfully for orderId: {}, Paytm Refund ID: {}, Status: {}", orderId, paytmRefundId, orderRefund.getStatus());

                return "Refund request processed successfully for orderId: " + orderId + ", Paytm Refund ID: " + orderRefund.getGatewayRefundId() + ", Status: " + orderRefund.getStatus();
            }
        } catch (Exception e) {
            orderRefund.setStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);
            //tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);
            orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_FAILED);

            coFeignClient.updateOrderStatus(orderDto);
            //transactionRepository.save(tx);
            refundRepository.save(orderRefund);
            throw new RuntimeException("CRITICAL: Failed to dispatch Paytm refund execution pipeline", e);
        }
        return "Refund request failed for orderId: " + orderId + ". Please check Paytm dashboard for details.";
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
            com.razorpay.Refund rzpRefund = razorpayClient.refunds.create(refundRequest);
            String rzpRefundId = rzpRefund.get("id");

            // 4. Update local state
            orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
            coFeignClient.updateOrderStatus(orderDto);

      /*  tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
        transactionRepository.save(tx);*/

            // 5. Audit the refund ledger
            OrderRefund orderRefund = DivPaymentMapper.mapToRefundOrder(orderDto, tx, rzpRefundId, reason);

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
    private DivPaymentInitiateResponse initiatePaytmPayment(DivPlaceOrderRequestDto placeOrderRequestDto) {

        ResponseEntity<DivOrderDto> placeOrderRequestDtoResponseEntity =
                coFeignClient.placeOrder(placeOrderRequestDto);

        DivOrderDto orderDto = placeOrderRequestDtoResponseEntity.getBody();

        log.info("Initiating Paytm payment for order: {}", orderDto.getOrderId());

        int amount = orderDto.getOrderTotalAmount().multiply(new java.math.BigDecimal(100)).intValue();
        PaymentTransaction transaction = DivPaymentMapper.mapToTransactions(orderDto, null,
                amount);

        transaction = transactionRepository.save(transaction);

        DivOrderDto divOrderDto =  coFeignClient.getOrder(transaction.getApplicationOrderId());
        divOrderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_PENDING);
        coFeignClient.updateOrderStatus(divOrderDto);

        try {

            Map<String, Object> body = new TreeMap<>();
            body.put("requestType", "Payment");
            body.put("mid", paytmMid.trim());
            body.put("websiteName", "WEBSTAGING");
            body.put("orderId", orderDto.getOrderId());
            // CHANGE 1: Safe replacement for path variable mapping
            String formattedCallback = "https://securestage.paytmpayments.co/api/div/payment/callback/"+ orderDto.getOrderId();
            body.put("callbackUrl", formattedCallback);

            body.put("channelId", "WEB");

            Map<String, String> txnAmountMap = new HashMap<>();
            // Convert back to string representation of rupee value for Paytm interface requirements
            double decimalAmount = amount / 100.0;
            txnAmountMap.put("value", String.format("%.2f", decimalAmount));
            txnAmountMap.put("currency", "INR");
            body.put("txnAmount", txnAmountMap);

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("custId", placeOrderRequestDto.getCustomerId().toString());
            body.put("userInfo", userInfo);

            System.out.println("=======================Paytm =========");
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(body);
            String checksum = PaytmChecksum.generateSignature(jsonBody, paytmMerchantKey);

            System.out.println("=======================checksum received ========="+checksum);

            Map<String, String> head = new HashMap<>();
            head.put("signature", checksum);

            Map<String, Object> paytmRequest = new HashMap<>();
            paytmRequest.put("body", body);
            paytmRequest.put("head", head);

            String finalJsonPayload = mapper.writeValueAsString(paytmRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(finalJsonPayload, headers);

            log.info("Sending Paytm initiation request for order: {} with payload: {}", orderDto.getOrderId(), paytmRequest);
            String mid = paytmMid.trim();

            // CHANGE 2: Build the required URL query parameters dynamically
            String finalUrlWithParams = paytmUrl + "?mid=" + mid + "&orderId=" + orderDto.getOrderId();

            // Fire the call using the fixed query string
            ResponseEntity<Map> response = restTemplate.postForEntity(finalUrlWithParams, entity, Map.class);

            log.info("=========================="+response.getBody());

            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> resultBody = (Map<String, Object>) responseBody.get("body");
            Map<String, Object> resultInfo = (Map<String, Object>) resultBody.get("resultInfo");
            log.info("=========================="+resultInfo);
            DivPaymentInitiateResponse apiResponse = new DivPaymentInitiateResponse();
            apiResponse.setOrderId(orderDto.getOrderId());
            apiResponse.setToPayAmount(orderDto.getOrderTotalAmount());

            if ("S".equals(resultInfo.get("resultStatus"))) {
                apiResponse.setPaytmTxnToken((String) resultBody.get("txnToken"));
            } /*else {
                throw new RuntimeException("Paytm initiation failed: " + resultInfo.get("resultMsg"));
            }*/
            return  apiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error executing Paytm connection handshake wrapper", e);
        }

    }

    @Transactional
    @Override
    public ResponseEntity<String> paytmPaymentCallback(String orderId, HttpServletRequest request)  {
        log.info("Received Paytm payment callback for orderId: {}", orderId);

        try{

            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> paytmParams = new HashMap<>();

            // Flatten parameters for easy access
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                paytmParams.put(entry.getKey(), entry.getValue()[0]);
            }

            // 1. Verify Checksum first (Crucial Step!)
            String paytmChecksum = paytmParams.get("CHECKSUMHASH");

            boolean isValidChecksum = PaytmChecksum.verifySignature((TreeMap<String, String>) paytmParams,
                    paytmMerchantKey, paytmChecksum);

            if (isValidChecksum) {
                String status = paytmParams.get("STATUS");

                if ("TXN_SUCCESS".equals(status)) {
                    // 2. Extract your Bank Reference Identifiers
                    String paytmTxnId = paytmParams.get("TXNID");       // Paytm's internal ID
                    String bankTxnId = paytmParams.get("BANKTXNID");   // This is your RRN / Bank Ref No
                    String bankName = paytmParams.get("BANKNAME");     // e.g., HDFC, SBI, WALLET
                    String responseCode = paytmParams.get("RESPCODE");          // Bank response code

                    // Log or save these parameters to your database columns for reconciliation
                    log.info("Payment successful via: {}", bankName);
                    log.info("Bank RRN / reference: {}", bankTxnId);

                    PaymentTransaction tx = transactionRepository.findByApplicationOrderId(orderId)
                            .orElseThrow(() -> new RuntimeException("Transaction record not found"));

                    tx.setGatewayPaymentId(paytmTxnId);
                    tx.setTxnRrn(bankTxnId);
                    tx.setBankName(bankName);
                    tx.setBankAuthCode(responseCode);
                    tx.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_SUCCESS);
                    transactionRepository.save(tx);

                    DivOrderDto divOrderDto =  coFeignClient.getOrder(orderId);
                    divOrderDto.setOrderStatus(DivAppConstants.START_PREPARING);
                    coFeignClient.updateOrderStatus(divOrderDto);

                    return ResponseEntity.ok("Success");
                }
            }

        }catch (Exception e){
            log.error("Error processing Paytm callback for orderId: {}. Error: {}", orderId, e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
        return ResponseEntity.status(500).body("Internal Server Error");
    }




}
