package com.jippy.division.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.*;
import com.jippy.division.entity.OrderRefund;
import com.jippy.division.entity.PaymentTransaction;

import com.jippy.division.feignClient.CoFeignClient;
import com.jippy.division.mapper.DivPaymentMapper;
import com.jippy.division.repositary.OrderRefundRepository;
import com.jippy.division.repositary.TransactionRepository;
import com.jippy.division.service.DivPaymentService;
import com.jippy.division.service.PayUService;
import com.paytm.pg.merchant.PaytmChecksum;
import com.razorpay.*;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Value("${payu.merchant-key}")
    private String payUMerchantKey;

    private final PayUService payUService;

    @Override
    public DivPaymentInitiateResponse initiatePayment(DivPlaceOrderRequestDto placeOrderRequestDto) throws RazorpayException {

        ResponseEntity<DivPaymentModesDto> paymentModesDtoResponseEntity =
                coFeignClient.getPaymentModeById(placeOrderRequestDto.getPaymentModeId());

        DivPaymentModesDto paymentModesDto = paymentModesDtoResponseEntity.getBody();
        if(paymentModesDto == null) {
            throw new RuntimeException("Payment mode not found for ID: " + placeOrderRequestDto.getPaymentModeId());
        }
        DivPaymentInitiateResponse response = new DivPaymentInitiateResponse();

        log.info("Payment mode for this order : {}",paymentModesDto.getPaymentMode());

        if(paymentModesDto.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_RAZOR_PAY)){
            response = initiateRazorPayPayment(placeOrderRequestDto);
            return response;
        }
        if(paymentModesDto.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_PAYTM)){
            response = initiatePaytmPayment(placeOrderRequestDto);
            return response;
        }
        if(paymentModesDto.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_PAYU)){
            response = initiatePayuPayment(placeOrderRequestDto);
            return response;
        }
        return response;
    }

      private DivPaymentInitiateResponse initiatePayuPayment(DivPlaceOrderRequestDto placeOrderRequestDto) {

          ResponseEntity<DivOrderDto> placeOrderRequestDtoResponseEntity =
                  coFeignClient.placeOrder(placeOrderRequestDto);

          //System.out.println("==============================="+placeOrderRequestDto.getDeliveryFee()+placeOrderRequestDto.getDeliveryFeeTax());

          DivOrderDto orderDto = placeOrderRequestDtoResponseEntity.getBody();

          log.info("Initiating payu payment for order: {}", placeOrderRequestDto.getOrderId());

          if(orderDto == null) {
              throw new RuntimeException("Order is not created because of some issue, Please try again " );
          }
         // System.out.println("=========================="+orderDto.toString());

          // Convert order total to subunits (Paise)
          int amountInPaise = orderDto.getOrderTotalAmount().multiply(new BigDecimal(100)).intValue();

          // Save transaction trace as PENDING
          PaymentTransaction transaction = DivPaymentMapper.mapToTransactions(orderDto, null, amountInPaise);
          transactionRepository.save(transaction);

          DivOrderDto divOrderDto =  coFeignClient.getOrder(transaction.getApplicationOrderId());
          divOrderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_PENDING);
          coFeignClient.updateOrderStatus(divOrderDto);

          ResponseEntity<DivCustomerResponseDto> customerResponseDtoResponseEntity =
                  coFeignClient.getCustomer(orderDto.getCustomerId());

          DivCustomerResponseDto customerResponseDto = new DivCustomerResponseDto();
          if(customerResponseDtoResponseEntity != null){
              customerResponseDto = customerResponseDtoResponseEntity.getBody();
          }

          // Amount formatted to 2 decimals as saved/calculated during initiation
          String amountStr = String.format("%.2f", transaction.getAmount() / 100.0);

          PaymentHashRequestDto hashRequestDto = new PaymentHashRequestDto();

          if( customerResponseDto != null){
              hashRequestDto.setEmail(customerResponseDto.getEmail());
              hashRequestDto.setCustomerName(customerResponseDto.getFirstName());
          }

          hashRequestDto.setAmount(amountStr);
          hashRequestDto.setTxnid(orderDto.getOrderId());
          hashRequestDto.setProductinfo("Food ordered #"+orderDto.getOrderId());

          Map<String, String> hashData = payUService.generatePaymentHash(hashRequestDto);

//          Map<String, String> payUParams = new HashMap<>();
//          payUParams.put("email", customerResponseDto.getEmail());
//          payUParams.put("firstname",customerResponseDto.getFirstName());
//          payUParams.put("productinfo", "Food ordered #" + orderDto.getOrderId());
//          payUParams.put("status","success");
//          payUParams.put("amount", orderDto.getOrderTotalAmount().toString());
//          payUParams.put("txnid",orderDto.getOrderId());
//          payUParams.put("key", payUMerchantKey);
//          System.out.println("=============================="+payUService.verifyResponseHash(payUParams));

          log.info("Payment initiated successfully for order: {} with payU Hash {}", orderDto.getOrderId(), hashData.get("paymentHash"));

          DivPaymentInitiateResponse response = new DivPaymentInitiateResponse();
          response.setOrderId(orderDto.getOrderId());
          response.setToPayAmount(orderDto.getOrderTotalAmount());
          response.setPayUHash(hashData.get("paymentHash"));
          response.setPayUMerchantKey(hashData.get("merchantKey"));

          return  response;

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
        int amountInPaise = orderDto.getOrderTotalAmount().multiply(new BigDecimal(100)).intValue();

        // Construct Razorpay payload
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", orderDto.getOrderId());

        System.out.println("==================call razor pay========");
        // Call Razorpay API
        Order rzpOrder = razorpayClient.orders.create(orderRequest);
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
    public boolean verifyAndCompletePayment(PaymentVerifyRequestDto request) {

        if(request.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_RAZOR_PAY)){
             return verifyAndCompleteRazorPayPayment(request);
        }
        if(request.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_PAYU)){
            return verifyAndCompletePayUPayment(request);
        }
        return false;
    }

    private boolean verifyAndCompletePayUPayment(PaymentVerifyRequestDto request) {
        try{

            Map<String, String> payUParams = convertResponseHashToMap(request);
            boolean isValidSignature = payUService.verifyResponseHash(payUParams);

            if (!isValidSignature) {
                log.warn("Invalid payU payment signature / hash mismatch!");
                throw new IllegalArgumentException("Invalid payU payment signature / hash mismatch!");
            }

            // 2. Fetch transaction record from DB
            PaymentTransaction transaction = transactionRepository
                    .findByApplicationOrderId(request.getApplicationOrderId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            // 3. Update DB record based on gateway status
            boolean isSuccess = (request.getPaymentMode().equalsIgnoreCase(DivAppConstants.PAYMENT_MODE_PAYU))
                    || ("success".equalsIgnoreCase(request.getPayuStatus()));

            if (isSuccess) {
                transaction.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_SUCCESS);
                transaction.setGatewayPaymentId(request.getPayuPaymentId());
                transaction.setGatewaySignature(request.getPayuHash());
                transaction.setTxnRrn(request.getBankRefNum());

                log.info("PayU Payment success : Updated record in db for orderId : {} ",request.getApplicationOrderId());
            } else {

                log.info("PayU Payment failed : Updated record in db for orderId : {} ",request.getApplicationOrderId());
                transaction.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_SUCCESS);
            }

            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            //After payment successful/failed update order record in db
            updateOrderStatus(request,isSuccess);

            return isSuccess;

        }catch (Exception e){
            log.error(e.getMessage());
        }
       return false;
    }

    void updateOrderStatus(PaymentVerifyRequestDto paymentVerifyRequestDto, boolean isSuccess){

        DivOrderDto orderDto = new DivOrderDto();

        if(isSuccess){
            orderDto.setOrderStatus(DivAppConstants.ORDER_PLACED);
        }else{
            orderDto.setOrderStatus(DivAppConstants.PAYMENT_STATUS_FAILED);
        }

        orderDto.setOrderId(paymentVerifyRequestDto.getApplicationOrderId());
        orderDto.setCustomerId(paymentVerifyRequestDto.getCustomerId());

        coFeignClient.updateOrderStatus(orderDto);
    }


    Map<String, String> convertResponseHashToMap(PaymentVerifyRequestDto paymentVerifyRequestDto) {
        if (paymentVerifyRequestDto == null || paymentVerifyRequestDto.getPayuHash() == null) {
            return new HashMap<>();
        }

        // Build map and delegate logic to existing Map method
        Map<String, String> payuParams = new HashMap<>();

        ResponseEntity<DivCustomerResponseDto> customerResponseDtoResponseEntity =
                coFeignClient.getCustomer(paymentVerifyRequestDto.getCustomerId());

        DivCustomerResponseDto customerResponseDto = new DivCustomerResponseDto();
        if(customerResponseDtoResponseEntity != null){
            customerResponseDto = customerResponseDtoResponseEntity.getBody();

            payuParams.put("email", customerResponseDto.getEmail());
            payuParams.put("firstname",customerResponseDto.getFirstName());
            payuParams.put("productinfo", "Food ordered #" + paymentVerifyRequestDto.getApplicationOrderId());
        }


        payuParams.put("hash", paymentVerifyRequestDto.getPayuHash());
        payuParams.put("status", paymentVerifyRequestDto.getPayuStatus());
        payuParams.put("amount", paymentVerifyRequestDto.getAmount() != null ? paymentVerifyRequestDto.getAmount() : "");
        payuParams.put("txnid", paymentVerifyRequestDto.getApplicationOrderId() != null ? paymentVerifyRequestDto.getApplicationOrderId() : "");
        payuParams.put("key", payUMerchantKey);

        return payuParams;
    }


    private boolean verifyAndCompleteRazorPayPayment(PaymentVerifyRequestDto request) {
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
                    orderDto.setOrderStatus(DivAppConstants.ORDER_PLACED);
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







    @Transactional
    private DivPaymentInitiateResponse initiatePaytmPayment(DivPlaceOrderRequestDto placeOrderRequestDto) {

        ResponseEntity<DivOrderDto> placeOrderRequestDtoResponseEntity =
                coFeignClient.placeOrder(placeOrderRequestDto);

        DivOrderDto orderDto = placeOrderRequestDtoResponseEntity.getBody();

        log.info("Initiating Paytm payment for order: {}", orderDto.getOrderId());

        int amount = orderDto.getOrderTotalAmount().multiply(new BigDecimal(100)).intValue();
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
                    divOrderDto.setOrderStatus(DivAppConstants.ORDER_PLACED);
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
