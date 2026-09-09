package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoOrderItem;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CoMerchantSettlementService;
import com.jippy.customerandorder.mapper.CoMerchantSettlementMapper;
import com.jippy.customerandorder.projection.CoOrderSettlementProjection;
import com.jippy.customerandorder.repository.CoOrderItemRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.function.Add;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Service implementation for merchant settlement
@Service
@RequiredArgsConstructor
@Slf4j
public class CoMerchantSettlementServiceImpl implements CoMerchantSettlementService {

    private final CoOrderRepository coOrdersRepository;

    private final CoOrderItemRepository coOrderItemsRepository;

    private final FMFeignClient coFmFeignClient;


    //     Fetch merchant settlement details between start date and end date
    @Override
    public List<CoMerchantSettlementOutletDto> getProductDetailsForMerchantSettlement
    (LocalDate startDate, LocalDate endDate) {

        log.info("Started fetching merchant settlement details from {} to {}", startDate,endDate);

//         Validate request dates
        validateDates(startDate,endDate);


//         Fetch delivered orders between dates start date and end date
        List<CoOrderSettlementProjection> orders =
                coOrdersRepository.getProductDetailsForMerchantSettlement(startDate,endDate);

        log.info("Total delivered orders fetched : {}", orders.size());

//         Throw exception if no orders found
        if (orders.isEmpty()) {

            log.error("No delivered orders found between {} and {}", startDate, endDate);

            throw new CoBadRequestException("No delivered orders found between "
                    + startDate + " and " + endDate);
        }

//         Store outlet wise settlements in map to avoid duplicate outlet details
//         and to manage orders list in outlet settlement
        Map<Integer, CoMerchantSettlementOutletDto> outletMap = new HashMap<>();

//       Process each order and set order details in outlet settlement
        for (CoOrderSettlementProjection order : orders) {

            log.info("Processing order id : {}", order.getOrderId());


//             Map order details instead of list because we need to set products list in order details
//             and it will be easy to manage products list in order details instead of outlet details
            CoMerchantSettlementResponseDto settlement =
                    CoMerchantSettlementMapper.toSettlementResponseDto(order);


//             Fetch outlet details from FM
            log.info("Calling FM outlet API for outlet id : {}", order.getOutletId());

            CoFmOutletDto outletDto = coFmFeignClient.getOutletById(order.getOutletId());
            log.info("FM Outlet Response: {}", outletDto);

//             Fetch area name from FM from CoFmOutletDto-> area id
            log.info("Calling FM area API for area id : {}", outletDto.getAreaId());

//             Fetch order items using order id for calculating total price
//             and fetching product details
            List<CoOrderItem> orderItems = coOrderItemsRepository.findByOrder_OrderId(order.getOrderId());

            log.info("Total products found for order {} : {}", order.getOrderId(), orderItems.size());

            List<CoMerchantSettlementProductDto> products = new ArrayList<>();

//             Process each product in order and set product details
//             in order details of outlet settlement
            for (CoOrderItem item : orderItems) {

                log.info("Calling FM product API for product id : {}", item.getProductId());


//               Fetch product details from FM for product name
                CoFmProductDto productDto = coFmFeignClient.getSettlementProductById(item.getProductId());

//                setting product details from items table in CO
                CoMerchantSettlementProductDto product = CoMerchantSettlementMapper.toProductDto(item);

//                 Set product name from FM(CoFmProductDto)
                product.setProductName(productDto.getProductName());
                products.add(product);
            }

//             Set products list to CoMerchantSettlementResponseDto
            settlement.setProducts(products);

//             Check outlet already exists
            CoMerchantSettlementOutletDto outletSettlement = outletMap.get(order.getOutletId());

//             Create new outlet settlement if outlet not present
            if (outletSettlement == null) {


                outletSettlement = CoMerchantSettlementMapper
                        .toOutletSettlementDto(order, outletDto);

//            adding response dto to MerchantSettlementOutletDto
//            because we need to set orders list in outlet settlement
                List<CoMerchantSettlementResponseDto> orderList = new ArrayList<>();

//                adding order details to order list of outlet settlement
                orderList.add(settlement);

                outletSettlement.setOrders(orderList);
//                 Store outlet in map
                outletMap.put(order.getOutletId(), outletSettlement);

            } else {

//                 Add settlement amount
                outletSettlement.setSettlementAmount
                        (outletSettlement.getSettlementAmount().add(order.getTotalPrice()));


//            Add orders into existing outlet
//            For ex : Add order under the same outlet
                outletSettlement.getOrders().add(settlement);
            }

            log.info("Completed processing order id : {}", order.getOrderId());
        }  // end of map

        log.info("Merchant settlement details fetched successfully");


//         Return outlet wise settlements
//        Return all outlet settlement objects stored in the map as a List.
        return new ArrayList<>(outletMap.values());
    }


    //     Validate start date and end date
    private void validateDates(LocalDate startDate,LocalDate endDate) {

//         Validate start date should not be greater than end date
         if (startDate.isAfter(endDate)) {

            log.error("Start date is greater than end date");

            throw new CoBadRequestException("Start date should not be greater than end date");
        }
    }
}
